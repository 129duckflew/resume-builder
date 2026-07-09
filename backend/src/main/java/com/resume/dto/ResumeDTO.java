package com.resume.dto;

public class ResumeDTO {

    private String id;

    private String title;

    private String content;

    private String themeId;

    private Float fontSize;

    private Float lineHeight;

    private String sectionSpacing;

    private String createdAt;

    private String updatedAt;

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
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
