package com.resume.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.ThemeDTO;
import com.resume.dto.VariableDeclaration;
import com.resume.entity.Theme;
import com.resume.repository.ThemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ThemeService {

    private static final Set<String> VALID_LAYOUTS = Set.of(
            "single", "sidebar-left", "sidebar-right", "header-bar");

    private final ThemeRepository themeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<VariableDeclaration> getVariables(String themeId) {
        return findById(themeId)
                .map(theme -> {
                    String schema = theme.getVariablesSchema();
                    if (schema == null) return Collections.<VariableDeclaration>emptyList();
                    try {
                        return objectMapper.readValue(schema,
                                new TypeReference<List<VariableDeclaration>>() {});
                    } catch (Exception e) {
                        return Collections.<VariableDeclaration>emptyList();
                    }
                })
                .orElse(null);
    }

    public List<Theme> findAll() {
        return themeRepository.findAllByOrderBySortOrderAsc();
    }

    public List<Theme> findAll(Long userId) {
        if (userId == null) {
            return themeRepository.findByBuiltInTrueOrderBySortOrderAsc();
        }
        return themeRepository.findByBuiltInTrueOrUserIdOrderBySortOrderAsc(userId);
    }

    public Optional<Theme> findById(String id) {
        return themeRepository.findById(id);
    }

    public Optional<Theme> findByIdAndUserId(String id, Long userId) {
        return themeRepository.findByIdAndUserId(id, userId);
    }

    public Theme createCustomTheme(ThemeDTO dto, Long userId) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Theme name is required");
        }
        String layout = dto.getLayout() != null ? dto.getLayout() : "single";
        if (!VALID_LAYOUTS.contains(layout)) {
            throw new IllegalArgumentException("Invalid layout: " + layout);
        }
        String id = "user-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8);
        Theme theme = new Theme();
        theme.setId(id);
        theme.setName(dto.getName().trim());
        theme.setDescription(dto.getDescription());
        theme.setBuiltIn(false);
        theme.setUserId(userId);
        theme.setLayout(layout);
        String css = dto.getCssContent();
        if (css != null) {
            theme.setCssContent(CssSanitizer.sanitize(css));
        }
        if (dto.getVariablesSchema() != null) {
            theme.setVariablesSchema(dto.getVariablesSchema());
        }
        return themeRepository.save(theme);
    }

    public Theme updateCustom(String id, ThemeDTO dto, Long userId) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theme not found: " + id));
        if (!theme.isBuiltIn() && !userId.equals(theme.getUserId())) {
            throw new SecurityException("Not authorized to modify this theme");
        }
        if (dto.getName() != null) {
            theme.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            theme.setDescription(dto.getDescription());
        }
        if (dto.getLayout() != null) {
            if (!VALID_LAYOUTS.contains(dto.getLayout())) {
                throw new IllegalArgumentException("Invalid layout: " + dto.getLayout());
            }
            theme.setLayout(dto.getLayout());
        }
        if (dto.getCssContent() != null) {
            theme.setCssContent(CssSanitizer.sanitize(dto.getCssContent()));
        }
        if (dto.getVariablesSchema() != null) {
            theme.setVariablesSchema(dto.getVariablesSchema());
        }
        return themeRepository.save(theme);
    }

    @Transactional
    public Theme updateDirect(String id, ThemeDTO dto) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theme not found: " + id));
        if (dto.getName() != null) theme.setName(dto.getName().trim());
        if (dto.getDescription() != null) theme.setDescription(dto.getDescription());
        if (dto.getLayout() != null) {
            if (!VALID_LAYOUTS.contains(dto.getLayout())) {
                throw new IllegalArgumentException("Invalid layout: " + dto.getLayout());
            }
            theme.setLayout(dto.getLayout());
        }
        if (dto.getCssContent() != null) {
            theme.setCssContent(CssSanitizer.sanitize(dto.getCssContent()));
        }
        if (dto.getVariablesSchema() != null) {
            theme.setVariablesSchema(dto.getVariablesSchema());
        }
        return themeRepository.save(theme);
    }

    public void deleteCustom(String id, Long userId) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theme not found: " + id));
        if (theme.isBuiltIn()) {
            throw new IllegalStateException("Cannot delete a built-in theme");
        }
        if (!userId.equals(theme.getUserId())) {
            throw new SecurityException("Not authorized to delete this theme");
        }
        themeRepository.delete(theme);
    }
}
