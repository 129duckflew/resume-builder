package com.resume.dto;

import java.util.Map;

public class ResumeStyleDTO {

    private Float fontSize;
    private Float lineHeight;
    private String sectionSpacing;
    private Map<String, String> customVariables;

    public Float getFontSize() { return fontSize; }
    public void setFontSize(Float fontSize) { this.fontSize = fontSize; }
    public Float getLineHeight() { return lineHeight; }
    public void setLineHeight(Float lineHeight) { this.lineHeight = lineHeight; }
    public String getSectionSpacing() { return sectionSpacing; }
    public void setSectionSpacing(String sectionSpacing) { this.sectionSpacing = sectionSpacing; }
    public Map<String, String> getCustomVariables() { return customVariables; }
    public void setCustomVariables(Map<String, String> customVariables) { this.customVariables = customVariables; }
}
