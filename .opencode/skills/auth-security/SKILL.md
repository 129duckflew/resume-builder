---
name: auth-security
description: Spring Security + JWT checklist. Use when touching SecurityConfig, JwtAuthFilter, JwtUtil, login/register endpoints, BCrypt, CORS, or authorization rules.
---

## Spring Security + JWT Checklist

- [ ] `SecurityConfig.permitAll()` 覆盖 `/api/auth/**` 和 `/api/themes/**`
- [ ] 其余路径配置 `.anyRequest().authenticated()`
- [ ] `SessionCreationPolicy.STATELESS`（无状态）
- [ ] `JwtAuthFilter extends OncePerRequestFilter` 提取 Bearer token
- [ ] `JwtUtil.generateToken(userId, username)` 生成复合 subject
- [ ] `SecurityContextHolder` 存储 `userId:username`
- [ ] Controller 通过 `currentUserId()` 从 SecurityContext 解析 userId
- [ ] Service 所有 CRUD 按 `userId` 过滤
- [ ] 密码使用 `BCryptPasswordEncoder`
- [ ] CORS 配置允许前端域名
- [ ] CSRF 禁用（stateless API 不需要）
