package com.resume.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.ResumeStyleDTO;
import com.resume.entity.ResumeStyle;
import com.resume.repository.ResumeStyleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class ResumeStyleService {

    private final ResumeStyleRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeStyleService(ResumeStyleRepository repository) {
        this.repository = repository;
    }

    public Optional<ResumeStyle> getStyle(String resumeId, String themeId) {
        return repository.findByResumeIdAndThemeId(resumeId, themeId);
    }

    @Transactional
    public ResumeStyle saveStyle(String resumeId, String themeId, ResumeStyleDTO dto) {
        ResumeStyle style = repository.findByResumeIdAndThemeId(resumeId, themeId)
                .orElseGet(() -> {
                    ResumeStyle s = new ResumeStyle();
                    s.setResumeId(resumeId);
                    s.setThemeId(themeId);
                    return s;
                });

        if (dto.getFontSize() != null) style.setFontSize(dto.getFontSize());
        if (dto.getLineHeight() != null) style.setLineHeight(dto.getLineHeight());
        if (dto.getSectionSpacing() != null) style.setSectionSpacing(dto.getSectionSpacing());
        if (dto.getCustomVariables() != null) {
            try {
                String json = objectMapper.writeValueAsString(dto.getCustomVariables());
                style.setCustomVariables(json);
            } catch (Exception e) {
                style.setCustomVariables("{}");
            }
        }

        return repository.save(style);
    }
}
