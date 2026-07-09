package com.resume.dto;

/**
 * DTO for creating or updating a custom theme.
 * All fields are nullable for PUT partial updates.
 */
public class ThemeDTO {

    private String name;
    private String description;
    private String cssContent;
    private String variablesSchema;
    private String layout;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCssContent() { return cssContent; }
    public void setCssContent(String cssContent) { this.cssContent = cssContent; }
    public String getVariablesSchema() { return variablesSchema; }
    public void setVariablesSchema(String variablesSchema) { this.variablesSchema = variablesSchema; }
    public String getLayout() { return layout; }
    public void setLayout(String layout) { this.layout = layout; }
}
