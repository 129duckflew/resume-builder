package com.resume.service;

import com.resume.entity.Theme;
import com.resume.repository.ThemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository repository;

    @Mock
    private ResourceLoader resourceLoader;

    private ThemeService service;

    @BeforeEach
    void setUp() {
        service = new ThemeService(repository, resourceLoader);
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
    void initBuiltInThemes_overwritesExistingThemes() {
        for (String id : List.of("classic", "modern", "minimal", "sidebar", "stackoverflow", "elegant", "compact")) {
            when(repository.findById(id)).thenReturn(Optional.of(new Theme()));
        }
        when(resourceLoader.getResource(anyString()))
                .thenReturn(new ByteArrayResource("css".getBytes()));

        service.initBuiltInThemes();

        verify(repository, times(7)).save(any());
    }

    @Test
    void initBuiltInThemes_createsMissingTheme_withResourceFallback() {
        when(repository.findById("classic")).thenReturn(Optional.empty());
        for (String id : List.of("modern", "minimal", "sidebar", "stackoverflow", "elegant", "compact")) {
            when(repository.findById(id)).thenReturn(Optional.of(new Theme()));
        }
        when(resourceLoader.getResource(anyString()))
                .thenReturn(new ByteArrayResource("body { color: black; }".getBytes()));

        service.initBuiltInThemes();

        verify(repository, times(7)).save(any());
    }
}
