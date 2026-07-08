package com.resume.service;

import com.resume.entity.SectionTemplate;
import com.resume.repository.SectionTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionTemplateServiceTest {

    @Mock
    private SectionTemplateRepository repository;

    private SectionTemplateService service;

    @BeforeEach
    void setUp() {
        service = new SectionTemplateService(repository);
    }

    @Test
    void initBuiltInTemplates_whenEmpty_createsDefaults() {
        when(repository.findByUserIdIsNullOrderBySortOrderAsc()).thenReturn(List.of());

        service.initBuiltInTemplates();

        verify(repository).saveAll(argThat(list -> {
            List<SectionTemplate> templates = (List<SectionTemplate>) list;
            return templates.size() == 8
                    && templates.get(0).getName().equals("Personal Info")
                    && templates.get(7).getName().equals("References");
        }));
    }

    @Test
    void initBuiltInTemplates_whenAlreadyExists_skips() {
        SectionTemplate existing = new SectionTemplate();
        existing.setName("Personal Info");
        when(repository.findByUserIdIsNullOrderBySortOrderAsc()).thenReturn(List.of(existing));

        service.initBuiltInTemplates();

        verify(repository, never()).saveAll(any());
    }

    @Test
    void getEffectiveTemplates_withUserId_mergesDefaultsAndUser() {
        SectionTemplate def = new SectionTemplate();
        def.setName("Personal Info");
        def.setSortOrder(1);

        SectionTemplate user = new SectionTemplate();
        user.setName("Custom");
        user.setUserId(1L);
        user.setSortOrder(2);

        when(repository.findByUserIdIsNullOrderBySortOrderAsc()).thenReturn(List.of(def));
        when(repository.findByUserIdOrderBySortOrderAsc(1L)).thenReturn(List.of(user));

        List<SectionTemplate> result = service.getEffectiveTemplates(1L);

        assertEquals(2, result.size());
        assertEquals("Personal Info", result.get(0).getName());
        assertEquals("Custom", result.get(1).getName());
    }

    @Test
    void create_savesAndReturns() {
        SectionTemplate template = new SectionTemplate();
        template.setName("My Template");

        when(repository.save(any())).thenAnswer(i -> {
            SectionTemplate saved = i.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        SectionTemplate result = service.create(template, 1L);

        assertEquals(99L, result.getId());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void update_ownTemplate_updates() {
        SectionTemplate existing = new SectionTemplate();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setName("Old");

        SectionTemplate updated = new SectionTemplate();
        updated.setName("New");
        updated.setIcon("star");
        updated.setPrompt("# New");
        updated.setSortOrder(5);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        SectionTemplate result = service.update(1L, updated, 1L);

        assertEquals("New", result.getName());
        assertEquals("star", result.getIcon());
        assertEquals("# New", result.getPrompt());
        assertEquals(5, result.getSortOrder());
    }

    @Test
    void update_otherUsersTemplate_throws() {
        SectionTemplate existing = new SectionTemplate();
        existing.setId(1L);
        existing.setUserId(2L);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class,
                () -> service.update(1L, new SectionTemplate(), 1L));
    }

    @Test
    void delete_ownTemplate_deletes() {
        SectionTemplate existing = new SectionTemplate();
        existing.setId(1L);
        existing.setUserId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L, 1L);

        verify(repository).delete(existing);
    }

    @Test
    void delete_otherUsersTemplate_throws() {
        SectionTemplate existing = new SectionTemplate();
        existing.setId(1L);
        existing.setUserId(2L);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class,
                () -> service.delete(1L, 1L));
    }
}
