package com.resume.service;

import com.resume.dto.DesensitizeRuleDTO;
import com.resume.entity.DesensitizeRule;
import com.resume.repository.DesensitizeRuleRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@Service
public class DesensitizeService {

    private static final Logger log = LoggerFactory.getLogger(DesensitizeService.class);

    private final DesensitizeRuleRepository repository;

    public DesensitizeService(DesensitizeRuleRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void initDefaultRules() {
        if (repository.findByUserIdIsNullOrderBySortOrderAsc().isEmpty()) {
            log.info("Initializing default desensitize rules");
            repository.saveAll(List.of(
                    createDefaultRule("phone", "Mobile phone number",
                            "(1[3-9]\\d)\\d{4}(\\d{4})", "$1****$2", 1),
                    createDefaultRule("email", "Email address",
                            "(\\w)[^@\\s]*@", "$1***@", 2)
            ));
        }
    }

    private DesensitizeRule createDefaultRule(String name, String description,
                                              String pattern, String replacement, int sortOrder) {
        DesensitizeRule rule = new DesensitizeRule();
        rule.setUserId(null);
        rule.setName(name);
        rule.setDescription(description);
        rule.setPattern(pattern);
        rule.setReplacement(replacement);
        rule.setEnabled(true);
        rule.setSortOrder(sortOrder);
        return rule;
    }

    public List<DesensitizeRuleDTO> getEffectiveRules(Long userId) {
        List<DesensitizeRule> defaults = repository.findByUserIdIsNullOrderBySortOrderAsc();
        List<DesensitizeRule> userRules = userId != null
                ? repository.findByUserIdOrderBySortOrderAsc(userId)
                : List.of();

        Map<String, DesensitizeRule> userRuleMap = userRules.stream()
                .collect(Collectors.toMap(DesensitizeRule::getName, r -> r, (a, b) -> a));

        List<DesensitizeRuleDTO> result = new ArrayList<>();

        for (DesensitizeRule def : defaults) {
            DesensitizeRule override = userRuleMap.get(def.getName());
            if (override != null) {
                result.add(toDTO(override, true));
                userRuleMap.remove(def.getName());
            } else {
                result.add(toDTO(def, true));
            }
        }

        for (DesensitizeRule custom : userRuleMap.values()) {
            result.add(toDTO(custom, false));
        }

        return result;
    }

    @Transactional
    public void saveUserRules(Long userId, List<DesensitizeRuleDTO> rules) {
        for (DesensitizeRuleDTO dto : rules) {
            try {
                Pattern.compile(dto.getPattern());
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(
                        "Invalid regex pattern in rule '" + dto.getName() + "': " + e.getMessage());
            }
        }

        repository.deleteByUserId(userId);

        List<DesensitizeRule> toSave = new ArrayList<>();
        for (DesensitizeRuleDTO dto : rules) {
            DesensitizeRule rule = new DesensitizeRule();
            rule.setUserId(userId);
            rule.setName(dto.getName());
            rule.setDescription(dto.getDescription());
            rule.setPattern(dto.getPattern());
            rule.setReplacement(dto.getReplacement());
            rule.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
            rule.setSortOrder(dto.getSortOrder());
            toSave.add(rule);
        }

        repository.saveAll(toSave);
    }

    public String apply(String content, Long userId) {
        if (content == null || content.isEmpty()) return content;

        List<DesensitizeRuleDTO> rules = getEffectiveRules(userId);

        String result = content;
        for (DesensitizeRuleDTO rule : rules) {
            if (Boolean.FALSE.equals(rule.getEnabled())) continue;
            try {
                Pattern p = Pattern.compile(rule.getPattern());
                result = p.matcher(result).replaceAll(rule.getReplacement());
            } catch (Exception e) {
                log.warn("Failed to apply desensitize rule '{}': {}", rule.getName(), e.getMessage());
            }
        }
        return result;
    }

    private DesensitizeRuleDTO toDTO(DesensitizeRule rule, boolean isDefaultRule) {
        DesensitizeRuleDTO dto = new DesensitizeRuleDTO();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setDescription(rule.getDescription());
        dto.setPattern(rule.getPattern());
        dto.setReplacement(rule.getReplacement());
        dto.setEnabled(rule.getEnabled());
        dto.setDefaultRule(isDefaultRule && rule.getUserId() == null);
        dto.setSortOrder(rule.getSortOrder());
        return dto;
    }
}
