package com.resume.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "theme_id", nullable = false)
    private String themeId = "classic";

    @Column(name = "font_size")
    private Float fontSize;

    @Column(name = "line_height")
    private Float lineHeight;

    @Column(name = "section_spacing")
    private String sectionSpacing = "normal";

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getThemeId() { return themeId; }
    public void setThemeId(String themeId) { this.themeId = themeId; }
    public Float getFontSize() { return fontSize; }
    public void setFontSize(Float fontSize) { this.fontSize = fontSize; }
    public Float getLineHeight() { return lineHeight; }
    public void setLineHeight(Float lineHeight) { this.lineHeight = lineHeight; }
    public String getSectionSpacing() { return sectionSpacing; }
    public void setSectionSpacing(String sectionSpacing) { this.sectionSpacing = sectionSpacing; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
