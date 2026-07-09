package com.resume.service;

import com.resume.dto.VariableDeclaration;
import com.resume.entity.Theme;
import com.resume.repository.ThemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThemeServiceVariablesTest {

    @Mock
    private ThemeRepository repository;

    @Mock
    private ResourceLoader resourceLoader;

    private ThemeService service;

    private static final List<String> ALL_THEMES = List.of(
            "classic", "modern", "minimal", "sidebar", "stackoverflow", "elegant", "compact",
            "sidebar-right", "header-bar", "jake", "academic", "swiss", "harvard"
    );

    @BeforeEach
    void setUp() {
        service = new ThemeService(repository, resourceLoader);
    }

    @Test
    void initBuiltInThemes_loadsVariablesSchema() {
        for (String id : ALL_THEMES) {
            when(repository.findById(id)).thenReturn(Optional.of(new Theme()));
        }
        for (String id : ALL_THEMES) {
            when(resourceLoader.getResource("classpath:themes/" + id + "/style.css"))
                    .thenReturn(new ByteArrayResource("body {}".getBytes(StandardCharsets.UTF_8)));
        }
        // Classic has variables, others don't
        when(resourceLoader.getResource("classpath:themes/classic/theme.json"))
                .thenReturn(new ByteArrayResource(
                        ("{\"variables\":[{\"name\":\"--primary-color\",\"type\":\"color\",\"default\":\"#000\"}]}")
                                .getBytes(StandardCharsets.UTF_8)));
        for (String id : List.of("modern", "minimal", "sidebar", "stackoverflow", "elegant", "compact", "sidebar-right", "header-bar", "jake", "academic", "swiss", "harvard")) {
            when(resourceLoader.getResource("classpath:themes/" + id + "/theme.json"))
                    .thenReturn(new ByteArrayResource("{}".getBytes(StandardCharsets.UTF_8)));
        }

        service.initBuiltInThemes();

        verify(repository, times(13)).save(any());
    }

    @Test
    void getVariables_returnsParsedList() {
        Theme theme = new Theme();
        theme.setId("classic");
        theme.setVariablesSchema(
                "[{\"name\":\"--primary-color\",\"type\":\"color\",\"default\":\"#000\",\"label\":\"Primary\",\"group\":\"Colors\"}]");
        when(repository.findById("classic")).thenReturn(Optional.of(theme));

        List<VariableDeclaration> vars = service.getVariables("classic");

        assertNotNull(vars);
        assertEquals(1, vars.size());
        assertEquals("--primary-color", vars.get(0).getName());
        assertEquals("color", vars.get(0).getType());
        assertEquals("#000", vars.get(0).getDefaultValue());
        assertEquals("Primary", vars.get(0).getLabel());
        assertEquals("Colors", vars.get(0).getGroup());
    }

    @Test
    void getVariables_withNoVariables_returnsEmptyList() {
        Theme theme = new Theme();
        theme.setId("classic");
        theme.setVariablesSchema(null);
        when(repository.findById("classic")).thenReturn(Optional.of(theme));

        List<VariableDeclaration> vars = service.getVariables("classic");

        assertNotNull(vars);
        assertTrue(vars.isEmpty());
    }

    @Test
    void getVariables_withNonExistingTheme_returnsNull() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        List<VariableDeclaration> vars = service.getVariables("missing");

        assertNull(vars);
    }
}
