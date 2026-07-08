package com.resume.service;

import com.resume.dto.DesensitizeRuleDTO;
import com.resume.entity.DesensitizeRule;
import com.resume.repository.DesensitizeRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesensitizeServiceTest {

    @Mock
    private DesensitizeRuleRepository repository;

    private DesensitizeService service;

    @BeforeEach
    void setUp() {
        service = new DesensitizeService(repository);
    }

    @Test
    void initDefaultRules_whenEmpty_createsDefaults() {
        when(repository.findByUserIdIsNullOrderBySortOrderAsc()).thenReturn(List.of());

        service.initDefaultRules();

        verify(repository).saveAll(argThat(rules -> {
            List<DesensitizeRule> list = (List<DesensitizeRule>) rules;
            return list.size() == 2
                    && list.get(0).getName().equals("phone")
                    && list.get(1).getName().equals("email");
        }));
    }

    @Test
    void initDefaultRules_whenAlreadyExists_skips() {
        DesensitizeRule existing = new DesensitizeRule();
        existing.setName("phone");
        when(repository.findByUserIdIsNullOrderBySortOrderAsc()).thenReturn(List.of(existing));

        service.initDefaultRules();

        verify(repository, never()).saveAll(any());
    }

    @Test
    void apply_masksPhoneNumber() {
        when(repository.findByUserIdIsNullOrderBySortOrderAsc()).thenReturn(
                List.of(defaultRule("phone", "(1[3-9]\\d)\\d{4}(\\d{4})", "$1****$2", true, 1))
        );
        when(repository.findByUserIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

        String result = service.apply("Contact: 13812345678", 1L);

        assertEquals("Contact: 138****5678", result);
    }

    @Test
    void apply_masksEmail() {
        when(repository.findByUserIdIsNullOrderBySortOrderAsc()).thenReturn(
                List.of(defaultRule("email", "(\\w)[^@\\s]*@", "$1***@", true, 1))
        );
        when(repository.findByUserIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

        String result = service.apply("Email: user@example.com", 1L);

        assertEquals("Email: u***@example.com", result);
    }

    @Test
    void apply_withDisabledRule_skips() {
        when(repository.findByUserIdIsNullOrderBySortOrderAsc()).thenReturn(
                List.of(defaultRule("phone", "(1[3-9]\\d)\\d{4}(\\d{4})", "$1****$2", false, 1))
        );
        when(repository.findByUserIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

        String result = service.apply("Contact: 13812345678", 1L);

        assertEquals("Contact: 13812345678", result);
    }

    @Test
    void apply_withNullContent_returnsNull() {
        assertNull(service.apply(null, 1L));
    }

    @Test
    void apply_withEmptyContent_returnsEmpty() {
        assertEquals("", service.apply("", 1L));
    }

    @Test
    void apply_withUserOverride_replacesDefault() {
        DesensitizeRule defaultRule = defaultRule("phone", "(1[3-9]\\d)\\d{4}(\\d{4})", "$1****$2", true, 1);
        DesensitizeRule userRule = userRule("phone", "(1[3-9]\\d)\\d{4}(\\d{4})", "$1XXXX$2", true, 1);
        when(repository.findByUserIdIsNullOrderBySortOrderAsc()).thenReturn(List.of(defaultRule));
        when(repository.findByUserIdOrderBySortOrderAsc(1L)).thenReturn(List.of(userRule));

        String result = service.apply("Contact: 13812345678", 1L);

        assertEquals("Contact: 138XXXX5678", result);
    }

    @Test
    void getEffectiveRules_mergesDefaultsAndUserRules() {
        DesensitizeRule defaultPhone = defaultRule("phone", "pattern1", "repl1", true, 1);
        DesensitizeRule defaultEmail = defaultRule("email", "pattern2", "repl2", true, 2);
        DesensitizeRule userCustom = userRule("wechat", "pattern3", "repl3", true, 3);

        when(repository.findByUserIdIsNullOrderBySortOrderAsc()).thenReturn(List.of(defaultPhone, defaultEmail));
        when(repository.findByUserIdOrderBySortOrderAsc(1L)).thenReturn(List.of(userCustom));

        List<DesensitizeRuleDTO> rules = service.getEffectiveRules(1L);

        assertEquals(3, rules.size());
        assertEquals("phone", rules.get(0).getName());
        assertTrue(rules.get(0).getDefaultRule());
        assertEquals("email", rules.get(1).getName());
        assertTrue(rules.get(1).getDefaultRule());
        assertEquals("wechat", rules.get(2).getName());
        assertFalse(rules.get(2).getDefaultRule());
    }

    @Test
    void getEffectiveRules_withUserOverride_replacesDefault() {
        DesensitizeRule defaultPhone = defaultRule("phone", "pattern1", "repl1", true, 1);
        DesensitizeRule userPhone = userRule("phone", "custom_pattern", "custom_repl", true, 1);

        when(repository.findByUserIdIsNullOrderBySortOrderAsc()).thenReturn(List.of(defaultPhone));
        when(repository.findByUserIdOrderBySortOrderAsc(1L)).thenReturn(List.of(userPhone));

        List<DesensitizeRuleDTO> rules = service.getEffectiveRules(1L);

        assertEquals(1, rules.size());
        assertEquals("phone", rules.get(0).getName());
        assertEquals("custom_pattern", rules.get(0).getPattern());
        assertFalse(rules.get(0).getDefaultRule());
    }

    @Test
    void saveUserRules_deletesOldAndSavesNew() {
        DesensitizeRuleDTO dto = new DesensitizeRuleDTO();
        dto.setName("phone");
        dto.setPattern("pattern");
        dto.setReplacement("repl");
        dto.setEnabled(true);
        dto.setSortOrder(1);

        service.saveUserRules(1L, List.of(dto));

        verify(repository).deleteByUserId(1L);
        verify(repository).saveAll(argThat(rules -> {
            List<DesensitizeRule> list = (List<DesensitizeRule>) rules;
            return list.size() == 1
                    && list.get(0).getName().equals("phone")
                    && list.get(0).getUserId().equals(1L);
        }));
    }

    @Test
    void saveUserRules_withInvalidPattern_throws() {
        DesensitizeRuleDTO dto = new DesensitizeRuleDTO();
        dto.setName("bad");
        dto.setPattern("[invalid");
        dto.setReplacement("repl");
        dto.setEnabled(true);
        dto.setSortOrder(1);

        assertThrows(IllegalArgumentException.class,
                () -> service.saveUserRules(1L, List.of(dto)));
        verify(repository, never()).deleteByUserId(any());
    }

    @Test
    void saveUserRules_withEmptyList_deletesOnly() {
        service.saveUserRules(1L, List.of());
        verify(repository).deleteByUserId(1L);
        verify(repository).saveAll(argThat(rules -> ((List<DesensitizeRule>) rules).isEmpty()));
    }

    @Test
    void saveUserRules_withNullEnabled_defaultsToTrue() {
        DesensitizeRuleDTO dto = new DesensitizeRuleDTO();
        dto.setName("phone");
        dto.setPattern("\\d+");
        dto.setReplacement("***");
        dto.setEnabled(null);
        dto.setSortOrder(1);

        service.saveUserRules(1L, List.of(dto));

        verify(repository).saveAll(argThat(rules -> {
            List<DesensitizeRule> list = (List<DesensitizeRule>) rules;
            return list.size() == 1 && list.get(0).getEnabled() == true;
        }));
    }

    private DesensitizeRule defaultRule(String name, String pattern, String replacement,
                                        boolean enabled, int sortOrder) {
        DesensitizeRule rule = new DesensitizeRule();
        rule.setName(name);
        rule.setPattern(pattern);
        rule.setReplacement(replacement);
        rule.setEnabled(enabled);
        rule.setSortOrder(sortOrder);
        return rule;
    }

    private DesensitizeRule userRule(String name, String pattern, String replacement,
                                     boolean enabled, int sortOrder) {
        DesensitizeRule rule = new DesensitizeRule();
        rule.setUserId(1L);
        rule.setName(name);
        rule.setPattern(pattern);
        rule.setReplacement(replacement);
        rule.setEnabled(enabled);
        rule.setSortOrder(sortOrder);
        return rule;
    }
}
