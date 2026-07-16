# MCP Server 排查记录

## 背景

MCP Server 作为 Spring Boot 模块已实现（12 个工具），部署到 k3s 后需要验证 MCP SSE 端点是否正常工作。

## 关键发现：`stdio: true` 导致自动配置完全跳过

**根因**：`McpWebMvcServerAutoConfiguration` 整个类带有 `@Conditional(McpServerStdioDisabledCondition.class)`。当 `application.yml` 中 `spring.ai.mcp.server.stdio=true` 时，**整个自动配置类被跳过**，包括：

1. `WebMvcSseServerTransportProvider` bean — 不创建
2. `mvcMcpRouterFunction` bean — 不创建（这个 bean 负责注册 SSE/Message 端点的路由）

我们的 `McpAuthConfig` 手动创建了 `WebMvcSseServerTransportProvider` bean，但**没有人创建 `RouterFunction` bean**，所以 SSE 端点根本没有注册到 Spring MVC 路由中。

## 排查过程

### 尝试 1：`AntPathRequestMatcher` in `authorizeHttpRequests`

```java
.requestMatchers(AntPathRequestMatcher.antMatcher("/mcp/**")).permitAll()
```

**结果**：403

**分析**：此时自动配置可能还在工作（RouterFunction 被创建），但 `AntPathRequestMatcher` 无法匹配 `RouterFunction` 注册的路径（RouterFunction 路径不经过 `HandlerMappingIntrospector`），请求落入 `anyRequest().authenticated()` → 403。

### 尝试 2：`WebSecurityCustomizer.ignoring()`

```java
@Bean
public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("/mcp/**");
}
```

**结果**：403

**分析**：`WebSecurityCustomizer.ignoring()` 同样依赖 `HandlerMappingIntrospector` 匹配路径，对 `RouterFunction` 注册的路径无效。

### 尝试 3：`securityMatcher()` 限制 SecurityFilterChain 范围

```java
http.securityMatcher("/api/**", "/s/**", "/actuator/**")
```

**结果**：404（不再是 403！）

**分析**：这证明了 403 的原因是 Spring Security 拦截了请求。`securityMatcher` 成功将 `/mcp/**` 排除在 Security 过滤链之外。但 404 说明 **SSE 端点根本没有注册**——因为 `RouterFunction` bean 从未被创建。

### 根因确认

反编译 `McpWebMvcServerAutoConfiguration.class`：

```
@Conditional(McpServerStdioDisabledCondition.class)  // ← 类级别条件
public class McpWebMvcServerAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    public WebMvcSseServerTransportProvider webMvcSseServerTransportProvider(...) { ... }

    @Bean
    public RouterFunction<ServerResponse> mcpRouterFunction(
            WebMvcSseServerTransportProvider transportProvider) {
        return transportProvider.getRouterFunction();  // ← 这个 bean 从未被创建
    }
}
```

当 `stdio: true` 时，整个类被跳过，包括 `mvcMcpRouterFunction`。

## 信息不清导致误判的原因

1. **之前的 debug 文档** (`docs/mcp-k8s-debug.md`) 记录了 403 问题，但没有深入分析 `stdio: true` 对自动配置的影响。文档停留在 "AntPathRequestMatcher 不匹配 RouterFunction 路径" 的层面，没有追问 "RouterFunction 是谁创建的"。

2. **403 vs 404 的混淆**：403 可能来自两种情况：
   - Spring Security 拦截了已注册的端点（RouterFunction 存在但被 Security 阻止）
   - Spring Security 拦截了未注册的端点（RouterFunction 不存在，请求落入默认 Security 链）
   
   两者返回相同的 403，但根因完全不同。只有排除 Security 干扰后（securityMatcher），404 才暴露了真正的问题。

3. **`stdio: true` 的语义不清**：`stdio: true` 在 Spring AI MCP 中意味着"作为 STDIO 进程运行"（由 MCP 客户端作为子进程启动），这会**完全禁用 Web MVC 传输**。但我们在 k8s 中需要的是 SSE 传输，两者互斥。

## 解决方案

在 `McpAuthConfig` 中手动创建 `RouterFunction` bean：

```java
@Bean
public RouterFunction<ServerResponse> mcpRouterFunction(WebMvcSseServerTransportProvider transportProvider) {
    return transportProvider.getRouterFunction();
}
```

同时保留 `securityMatcher()` 方案（将 `/mcp/**` 排除在 Security 过滤链之外），因为 MCP 有自己的 `securityValidator` 做 API Key 校验。

## 最终验证结果

```
=== SSE endpoint test (with auth) ===
event:endpoint
data:/mcp/message?sessionId=5826e659-2a9b-4a9a-89af-f52bc566d6bf
HTTP 200

=== Auth validation test (without auth) ===
Missing Authorization header
HTTP 401
```

- ✅ SSE 端点正常返回 session ID
- ✅ 无 auth 时返回 401
- ✅ MCP securityValidator 正确校验 API Key

## 关于 `stdio` 配置

- `stdio: true` + SSE 传输 = **需要手动创建 RouterFunction**
- `stdio: false` + SSE 传输 = **自动配置处理一切**
- k8s 部署场景应使用 `stdio: false`（SSE 模式），本地 STDIO 场景才需要 `stdio: true`
