package com.resume.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider mcpToolProvider(McpResumeTools resumeTools, McpThemeTools themeTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(resumeTools, themeTools)
                .build();
    }
}
