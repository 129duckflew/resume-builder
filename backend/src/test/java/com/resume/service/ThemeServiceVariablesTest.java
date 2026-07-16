package com.resume.service;

import com.resume.dto.VariableDeclaration;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThemeServiceVariablesTest {

    @Mock
    private ThemeRepository repository;

    private ThemeService service;

    @BeforeEach
    void setUp() {
        service = new ThemeService(repository);
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
