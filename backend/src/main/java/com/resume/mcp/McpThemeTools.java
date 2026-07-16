package com.resume.mcp;

import com.resume.dto.ThemeDTO;
import com.resume.entity.Theme;
import com.resume.service.ThemeService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpThemeTools {

    private final ThemeService themeService;

    public McpThemeTools(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Tool(name = "list_themes", description = "List all themes (built-in and custom)")
    public List<Theme> listThemes() {
        return themeService.findAll();
    }

    @Tool(name = "get_theme", description = "Get a theme by its ID with full details including CSS")
    public Theme getTheme(
            @ToolParam(description = "Theme ID (e.g. classic, modern, sidebar)") String id) {
        return themeService.findById(id)
                .orElseThrow(() -> new RuntimeException("Theme not found: " + id));
    }

    @Tool(name = "update_theme", description = "Update a theme (including built-in themes). Only provided fields are changed.")
    public Theme updateTheme(
            @ToolParam(description = "Theme ID") String id,
            @ToolParam(description = "New theme name", required = false) String name,
            @ToolParam(description = "New description", required = false) String description,
            @ToolParam(description = "CSS content", required = false) String cssContent,
            @ToolParam(description = "Layout type (single, sidebar-left, sidebar-right, header-bar)", required = false) String layout,
            @ToolParam(description = "Variables schema JSON", required = false) String variablesSchema) {
        ThemeDTO dto = new ThemeDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setCssContent(cssContent);
        dto.setLayout(layout);
        dto.setVariablesSchema(variablesSchema);
        return themeService.updateDirect(id, dto);
    }
}
