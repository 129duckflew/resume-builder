package com.resume.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import org.springframework.ai.mcp.server.autoconfigure.McpServerProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.Map;

@Configuration
public class McpAuthConfig {

    @Value("${app.mcp.api-key:}")
    private String mcpApiKey;

    @Bean
    public WebMvcSseServerTransportProvider webMvcSseServerTransportProvider(
            ObjectMapper objectMapper,
            McpServerProperties properties) {
        return WebMvcSseServerTransportProvider.builder()
            .jsonMapper(new JacksonMcpJsonMapper(objectMapper))
            .baseUrl(properties.getBaseUrl())
            .sseEndpoint(properties.getSseEndpoint())
            .messageEndpoint(properties.getSseMessageEndpoint())
            .contextExtractor(serverRequest -> {
                String auth = serverRequest.headers().firstHeader("Authorization");
                return McpTransportContext.create(Map.of("authorization", auth != null ? auth : ""));
            })
            .securityValidator(headers -> {
                if (mcpApiKey != null && !mcpApiKey.isEmpty()) {
                    List<String> authHeaders = headers.get("Authorization");
                    if (authHeaders == null || authHeaders.isEmpty()) {
                        throw new ServerTransportSecurityException(401, "Missing Authorization header");
                    }
                    String token = authHeaders.get(0);
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }
                    if (!mcpApiKey.equals(token)) {
                        throw new ServerTransportSecurityException(401, "Invalid API key");
                    }
                }
            })
            .build();
    }
}
