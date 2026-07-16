package com.resume.mcp;

import com.resume.dto.ThemeDTO;
import com.resume.entity.Theme;
import com.resume.service.ThemeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpThemeToolsTest {

    @Mock
    private ThemeService themeService;

    private McpThemeTools tools;

    @BeforeEach
    void setUp() {
        tools = new McpThemeTools(themeService);
    }

    @Test
    void listThemes_shouldReturnAllThemes() {
        when(themeService.findAll()).thenReturn(List.of(new Theme(), new Theme()));

        List<Theme> result = tools.listThemes();

        assertThat(result).hasSize(2);
    }

    @Test
    void getTheme_shouldReturnTheme() {
        Theme theme = new Theme();
        theme.setId("modern");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        Theme result = tools.getTheme("modern");

        assertThat(result.getId()).isEqualTo("modern");
    }

    @Test
    void getTheme_notFound_shouldThrow() {
        when(themeService.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tools.getTheme("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateTheme_shouldDelegateToUpdateDirect() {
        Theme expected = new Theme();
        expected.setId("classic");
        when(themeService.updateDirect(eq("classic"), any(ThemeDTO.class))).thenReturn(expected);

        Theme result = tools.updateTheme("classic", "New Classic", null, "body {}", null, null);

        assertThat(result.getId()).isEqualTo("classic");
        verify(themeService).updateDirect(eq("classic"), any(ThemeDTO.class));
    }
}
