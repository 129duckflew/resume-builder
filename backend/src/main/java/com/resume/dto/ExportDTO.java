package com.resume.dto;

public class ExportDTO {

    private String resumeId;
    private boolean smartOnePage = false;

    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }
    public boolean isSmartOnePage() { return smartOnePage; }
    public void setSmartOnePage(boolean smartOnePage) { this.smartOnePage = smartOnePage; }
}
