package com.resume.service;

import com.resume.entity.Theme;
import com.resume.repository.ThemeRepository;
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
class ThemeServiceTest {

    @Mock
    private ThemeRepository repository;

    private ThemeService service;

    @BeforeEach
    void setUp() {
        service = new ThemeService(repository);
    }

    @Test
    void findAll_returnsAllThemes() {
        var t1 = new Theme();
        t1.setId("a");
        var t2 = new Theme();
        t2.setId("b");
        when(repository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(t1, t2));

        List<Theme> result = service.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findById_withExistingId_returnsTheme() {
        var theme = new Theme();
        theme.setId("classic");
        when(repository.findById("classic")).thenReturn(Optional.of(theme));

        Optional<Theme> result = service.findById("classic");

        assertTrue(result.isPresent());
        assertEquals("classic", result.get().getId());
    }

    @Test
    void findById_withNonExistingId_returnsEmpty() {
        when(repository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<Theme> result = service.findById("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_withUserId_returnsBuiltInAndCustom() {
        var builtIn = new Theme();
        builtIn.setId("classic");
        builtIn.setBuiltIn(true);
        var custom = new Theme();
        custom.setId("user-1-abc");
        custom.setBuiltIn(false);
        custom.setUserId(1L);

        when(repository.findByBuiltInTrueOrUserIdOrderBySortOrderAsc(1L)).thenReturn(List.of(builtIn, custom));

        List<Theme> result = service.findAll(1L);

        assertEquals(2, result.size());
    }

    @Test
    void findAll_withoutUserId_returnsOnlyBuiltIn() {
        var builtIn = new Theme();
        builtIn.setId("classic");
        builtIn.setBuiltIn(true);

        when(repository.findByBuiltInTrueOrderBySortOrderAsc()).thenReturn(List.of(builtIn));

        List<Theme> result = service.findAll((Long) null);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isBuiltIn());
    }

    @Test
    void createCustomTheme_createsWithCorrectIdFormat() {
        com.resume.dto.ThemeDTO dto = new com.resume.dto.ThemeDTO();
        dto.setName("My Theme");
        dto.setLayout("sidebar-left");

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Theme result = service.createCustomTheme(dto, 1L);

        assertTrue(result.getId().startsWith("user-1-"));
        assertFalse(result.isBuiltIn());
        assertEquals(1L, result.getUserId());
        assertEquals("sidebar-left", result.getLayout());
        assertEquals("My Theme", result.getName());
    }

    @Test
    void createCustomTheme_sanitizesCss() {
        com.resume.dto.ThemeDTO dto = new com.resume.dto.ThemeDTO();
        dto.setName("Test");
        dto.setCssContent("body { color: red; }\n@import url('evil.css');");

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Theme result = service.createCustomTheme(dto, 1L);

        assertFalse(result.getCssContent().contains("@import"));
        assertTrue(result.getCssContent().contains("color: red"));
    }

    @Test
    void createCustomTheme_requiresName() {
        com.resume.dto.ThemeDTO dto = new com.resume.dto.ThemeDTO();
        dto.setName("");
        assertThrows(IllegalArgumentException.class, () -> service.createCustomTheme(dto, 1L));
    }

    @Test
    void updateCustom_updatesFields() {
        Theme existing = new Theme();
        existing.setId("user-1-abc");
        existing.setBuiltIn(false);
        existing.setUserId(1L);
        existing.setName("Old Name");
        existing.setLayout("single");

        when(repository.findById("user-1-abc")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        com.resume.dto.ThemeDTO dto = new com.resume.dto.ThemeDTO();
        dto.setName("New Name");
        dto.setLayout("sidebar-right");

        Theme result = service.updateCustom("user-1-abc", dto, 1L);

        assertEquals("New Name", result.getName());
        assertEquals("sidebar-right", result.getLayout());
    }

    @Test
    void updateCustom_notOwner_throws() {
        Theme existing = new Theme();
        existing.setId("user-1-abc");
        existing.setBuiltIn(false);
        existing.setUserId(2L);

        when(repository.findById("user-1-abc")).thenReturn(Optional.of(existing));

        com.resume.dto.ThemeDTO dto = new com.resume.dto.ThemeDTO();
        assertThrows(SecurityException.class, () -> service.updateCustom("user-1-abc", dto, 1L));
    }

    @Test
    void updateCustom_builtIn_succeeds() {
        Theme existing = new Theme();
        existing.setId("classic");
        existing.setBuiltIn(true);

        when(repository.findById("classic")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        com.resume.dto.ThemeDTO dto = new com.resume.dto.ThemeDTO();
        dto.setName("Updated Classic");

        Theme result = service.updateCustom("classic", dto, 1L);

        assertEquals("Updated Classic", result.getName());
    }

    @Test
    void deleteCustom_deletesTheme() {
        Theme existing = new Theme();
        existing.setId("user-1-abc");
        existing.setBuiltIn(false);
        existing.setUserId(1L);

        when(repository.findById("user-1-abc")).thenReturn(Optional.of(existing));
        doNothing().when(repository).delete(existing);

        service.deleteCustom("user-1-abc", 1L);

        verify(repository).delete(existing);
    }

    @Test
    void deleteCustom_notOwner_throws() {
        Theme existing = new Theme();
        existing.setId("user-1-abc");
        existing.setBuiltIn(false);
        existing.setUserId(2L);

        when(repository.findById("user-1-abc")).thenReturn(Optional.of(existing));

        assertThrows(SecurityException.class, () -> service.deleteCustom("user-1-abc", 1L));
    }

    @Test
    void deleteCustom_builtIn_throws() {
        Theme existing = new Theme();
        existing.setId("classic");
        existing.setBuiltIn(true);

        when(repository.findById("classic")).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> service.deleteCustom("classic", 1L));
    }
}
