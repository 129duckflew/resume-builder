package com.resume.service;

import com.resume.entity.SectionTemplate;
import com.resume.repository.SectionTemplateRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SectionTemplateService {

    private static final Logger log = LoggerFactory.getLogger(SectionTemplateService.class);

    private final SectionTemplateRepository repository;

    public SectionTemplateService(SectionTemplateRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void initBuiltInTemplates() {
        if (repository.findByUserIdIsNullOrderBySortOrderAsc().isEmpty()) {
            log.info("Initializing built-in section templates");
            repository.saveAll(List.of(
                    createTemplate("Personal Info", "user",
                            "# Personal Info\n\nName: \nPhone: \nEmail: \nLinkedIn: " +
                            "\nGitHub: \nPortfolio: ", 1),
                    createTemplate("Work Experience", "briefcase",
                            "# Work Experience\n\n## Company Name | Location\n" +
                            "*Job Title* | Start – End\n\n- Responsibility 1\n" +
                            "- Responsibility 2\n", 2),
                    createTemplate("Education", "graduation-cap",
                            "# Education\n\n## School Name | Location\n" +
                            "*Degree in Major* | Start – End\n\n- GPA: \n- Honors: \n" +
                            "- Relevant coursework: \n", 3),
                    createTemplate("Skills", "code",
                            "# Skills\n\n- Skill 1\n- Skill 2\n- Skill 3\n" +
                            "- Skill 4\n- Skill 5\n", 4),
                    createTemplate("Projects", "folder",
                            "# Projects\n\n## Project Name\n\n- Technology: \n" +
                            "- Role: \n- Description: \n", 5),
                    createTemplate("Certificates", "award",
                            "# Certificates\n\n- Certificate Name | Issuer | Year\n", 6),
                    createTemplate("Languages", "globe",
                            "# Languages\n\n- Language 1 (Native)\n- Language 2 (Fluent)\n" +
                            "- Language 3 (Intermediate)\n", 7),
                    createTemplate("References", "users",
                            "# References\n\nAvailable upon request.\n", 8)
            ));
        }
    }

    private SectionTemplate createTemplate(String name, String icon, String prompt, int sortOrder) {
        SectionTemplate t = new SectionTemplate();
        t.setUserId(null);
        t.setName(name);
        t.setIcon(icon);
        t.setPrompt(prompt);
        t.setSortOrder(sortOrder);
        return t;
    }

    public List<SectionTemplate> getEffectiveTemplates(Long userId) {
        List<SectionTemplate> defaults = repository.findByUserIdIsNullOrderBySortOrderAsc();
        if (userId == null) return defaults;
        List<SectionTemplate> userTemplates = repository.findByUserIdOrderBySortOrderAsc(userId);
        List<SectionTemplate> result = new ArrayList<>(defaults);
        result.addAll(userTemplates);
        return result;
    }

    @Transactional
    public SectionTemplate create(SectionTemplate template, Long userId) {
        template.setUserId(userId);
        return repository.save(template);
    }

    @Transactional
    public SectionTemplate update(Long id, SectionTemplate updated, Long userId) {
        SectionTemplate existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        if (!userId.equals(existing.getUserId())) {
            throw new RuntimeException("Not authorized");
        }
        existing.setName(updated.getName());
        existing.setIcon(updated.getIcon());
        existing.setPrompt(updated.getPrompt());
        existing.setSortOrder(updated.getSortOrder());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        SectionTemplate existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        if (!userId.equals(existing.getUserId())) {
            throw new RuntimeException("Not authorized");
        }
        repository.delete(existing);
    }
}
