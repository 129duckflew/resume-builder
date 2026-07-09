package com.resume.dto;

import java.time.LocalDateTime;

public class VersionMeta {

    private Integer versionNumber;
    private String title;
    private LocalDateTime createdAt;

    public VersionMeta() {}

    public VersionMeta(Integer versionNumber, String title, LocalDateTime createdAt) {
        this.versionNumber = versionNumber;
        this.title = title;
        this.createdAt = createdAt;
    }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
