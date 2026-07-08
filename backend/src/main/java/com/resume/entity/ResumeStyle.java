package com.resume.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "resume_styles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"resume_id", "theme_id"})
})
public class ResumeStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resume_id", nullable = false, length = 36)
    private String resumeId;

    @Column(name = "theme_id", nullable = false, length = 50)
    private String themeId;

    @Column(name = "font_size")
    private Float fontSize;

    @Column(name = "line_height")
    private Float lineHeight;

    @Column(name = "section_spacing", length = 20)
    private String sectionSpacing;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }
    public String getThemeId() { return themeId; }
    public void setThemeId(String themeId) { this.themeId = themeId; }
    public Float getFontSize() { return fontSize; }
    public void setFontSize(Float fontSize) { this.fontSize = fontSize; }
    public Float getLineHeight() { return lineHeight; }
    public void setLineHeight(Float lineHeight) { this.lineHeight = lineHeight; }
    public String getSectionSpacing() { return sectionSpacing; }
    public void setSectionSpacing(String sectionSpacing) { this.sectionSpacing = sectionSpacing; }
}
