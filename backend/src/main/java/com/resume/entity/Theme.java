package com.resume.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "themes")
public class Theme {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "css_content", columnDefinition = "TEXT")
    private String cssContent;

    @Column(name = "is_built_in")
    private boolean builtIn;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "variables_schema", columnDefinition = "TEXT")
    private String variablesSchema;

    @Column(columnDefinition = "varchar(20) not null default 'single'")
    private String layout = "single";

    @Column(name = "user_id")
    private Long userId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCssContent() { return cssContent; }
    public void setCssContent(String cssContent) { this.cssContent = cssContent; }
    public boolean isBuiltIn() { return builtIn; }
    public void setBuiltIn(boolean builtIn) { this.builtIn = builtIn; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getVariablesSchema() { return variablesSchema; }
    public void setVariablesSchema(String variablesSchema) { this.variablesSchema = variablesSchema; }
    public String getLayout() { return layout; }
    public void setLayout(String layout) { this.layout = layout; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
