package com.resume.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "section_templates")
public class SectionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String icon;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
