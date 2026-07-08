package com.resume.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_versions", indexes = {
        @Index(name = "idx_version_resume_id", columnList = "resume_id"),
        @Index(name = "idx_version_resume_version", columnList = "resume_id, version_number", unique = true)
})
public class ResumeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resume_id", nullable = false, length = 36)
    private String resumeId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "theme_id", nullable = false)
    private String themeId;

    @Column(name = "font_size")
    private Float fontSize;

    @Column(name = "line_height")
    private Float lineHeight;

    @Column(name = "section_spacing")
    private String sectionSpacing;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
}
