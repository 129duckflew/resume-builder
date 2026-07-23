package com.resume.mcp;

import tools.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import org.springframework.ai.mcp.server.webmvc.transport.WebMvcStreamableServerTransportProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import java.util.List;
import java.util.Map;

@Configuration
public class McpAuthConfig {

    @Value("${app.mcp.api-key:}")
    private String mcpApiKey;

    @Bean
    public WebMvcStreamableServerTransportProvider webMvcStreamableServerTransportProvider(
            @Value("${spring.ai.mcp.server.streamable-http.mcp-endpoint:/mcp}") String mcpEndpoint,
            JsonMapper jsonMapper) {
        return WebMvcStreamableServerTransportProvider.builder()
            .jsonMapper(new JacksonMcpJsonMapper(jsonMapper))
            .mcpEndpoint(mcpEndpoint)
            .securityValidator(headers -> {
                if (mcpApiKey != null && !mcpApiKey.isEmpty()) {
                    String token = null;
                    for (var entry : headers.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase("authorization")) {
                            List<String> values = entry.getValue();
                            if (values != null && !values.isEmpty()) {
                                token = values.get(0);
                            }
                            break;
                        }
                    }
                    if (token == null) {
                        throw new ServerTransportSecurityException(401, "Missing Authorization header");
                    }
                    if (token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }
                    if (!mcpApiKey.equals(token)) {
                        throw new ServerTransportSecurityException(401, "Invalid API key");
                    }
                }
            })
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> mcpRouterFunction(WebMvcStreamableServerTransportProvider transportProvider) {
        return transportProvider.getRouterFunction();
    }
}
