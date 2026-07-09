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

    @Deprecated
    @Column(name = "font_size")
    private Float fontSize;

    @Deprecated
    @Column(name = "line_height")
    private Float lineHeight;

    @Deprecated
    @Column(name = "section_spacing", length = 20)
    private String sectionSpacing;

    @Column(name = "custom_variables", columnDefinition = "TEXT")
    private String customVariables;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }
    public String getThemeId() { return themeId; }
    public void setThemeId(String themeId) { this.themeId = themeId; }
    /** @deprecated Use customVariables map instead (--font-size key) */
    @Deprecated
    public Float getFontSize() { return fontSize; }
    @Deprecated
    public void setFontSize(Float fontSize) { this.fontSize = fontSize; }
    /** @deprecated Use customVariables map instead (--line-height key) */
    @Deprecated
    public Float getLineHeight() { return lineHeight; }
    @Deprecated
    public void setLineHeight(Float lineHeight) { this.lineHeight = lineHeight; }
    /** @deprecated Use customVariables map instead (--section-spacing key) */
    @Deprecated
    public String getSectionSpacing() { return sectionSpacing; }
    @Deprecated
    public void setSectionSpacing(String sectionSpacing) { this.sectionSpacing = sectionSpacing; }
    public String getCustomVariables() { return customVariables; }
    public void setCustomVariables(String customVariables) { this.customVariables = customVariables; }
}
