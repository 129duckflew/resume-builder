package com.resume.mcp;

import com.resume.dto.ResumeDTO;
import com.resume.dto.VersionDiffResponse;
import com.resume.entity.Resume;
import com.resume.entity.ResumeVersion;
import com.resume.service.ResumeService;
import com.resume.service.ResumeVersionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpResumeTools {

    private final ResumeService resumeService;
    private final ResumeVersionService versionService;

    public McpResumeTools(ResumeService resumeService,
                          ResumeVersionService versionService) {
        this.resumeService = resumeService;
        this.versionService = versionService;
    }

    @Tool(name = "list_resumes", description = "List all resumes, optionally filtered by user ID")
    public List<Resume> listResumes(
            @ToolParam(description = "Optional user ID to filter resumes by") Long userId) {
        if (userId != null) {
            return resumeService.findByUserId(userId);
        }
        return resumeService.findAll();
    }

    @Tool(name = "get_resume", description = "Get a resume by its ID")
    public Resume getResume(
            @ToolParam(description = "Resume ID") String id) {
        return resumeService.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + id));
    }

    @Tool(name = "create_resume", description = "Create a new resume for a user")
    public Resume createResume(
            @ToolParam(description = "User ID who will own this resume") Long userId,
            @ToolParam(description = "Resume title", required = false) String title,
            @ToolParam(description = "Markdown content", required = false) String content,
            @ToolParam(description = "Theme ID (e.g. classic, modern)", required = false) String themeId) {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setThemeId(themeId);
        return resumeService.create(dto, userId);
    }

    @Tool(name = "update_resume", description = "Update a resume partially. Only provided fields are changed.")
    public Resume updateResume(
            @ToolParam(description = "Resume ID") String id,
            @ToolParam(description = "New title", required = false) String title,
            @ToolParam(description = "New markdown content", required = false) String content,
            @ToolParam(description = "New theme ID", required = false) String themeId,
            @ToolParam(description = "Font size", required = false) Float fontSize,
            @ToolParam(description = "Line height", required = false) Float lineHeight,
            @ToolParam(description = "Section spacing (e.g. normal, compact, relaxed)", required = false) String sectionSpacing) {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setThemeId(themeId);
        dto.setFontSize(fontSize);
        dto.setLineHeight(lineHeight);
        dto.setSectionSpacing(sectionSpacing);
        return resumeService.updateDirect(id, dto);
    }

    @Tool(name = "delete_resume", description = "Delete a resume permanently")
    public void deleteResume(
            @ToolParam(description = "Resume ID") String id) {
        resumeService.deleteDirect(id);
    }

    @Tool(name = "list_versions", description = "List all version snapshots for a resume")
    public List<ResumeVersion> listVersions(
            @ToolParam(description = "Resume ID") String resumeId) {
        return versionService.getVersions(resumeId);
    }

    @Tool(name = "get_version", description = "Get a specific version snapshot")
    public ResumeVersion getVersion(
            @ToolParam(description = "Resume ID") String resumeId,
            @ToolParam(description = "Version number (1, 2, 3...)") int version) {
        return versionService.getVersion(resumeId, version);
    }

    @Tool(name = "restore_version", description = "Restore a previous version as the current resume content")
    public Resume restoreVersion(
            @ToolParam(description = "Resume ID") String resumeId,
            @ToolParam(description = "Version number to restore") int version) {
        Resume restored = versionService.restoreVersion(resumeId, version);
        return resumeService.restoreFromVersionDirect(restored);
    }

    @Tool(name = "diff_versions", description = "Compare two versions and return a structured diff")
    public VersionDiffResponse diffVersions(
            @ToolParam(description = "Resume ID") String resumeId,
            @ToolParam(description = "First version number") int versionA,
            @ToolParam(description = "Second version number") int versionB) {
        return versionService.getDiff(resumeId, versionA, versionB);
    }
}
