package com.resume.controller;

import com.resume.entity.Theme;
import com.resume.service.ThemeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ThemeService themeService;

    @Test
    void list_returnsThemes() throws Exception {
        var theme = new Theme();
        theme.setId("classic");
        theme.setName("Classic");
        theme.setDescription("Desc");

        when(themeService.findAll()).thenReturn(List.of(theme));

        mockMvc.perform(get("/api/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("classic"))
                .andExpect(jsonPath("$[0].name").value("Classic"));
    }

    @Test
    void get_withExistingId_returnsTheme() throws Exception {
        var theme = new Theme();
        theme.setId("modern");
        theme.setName("Modern");

        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        mockMvc.perform(get("/api/themes/modern"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Modern"));
    }

    @Test
    void get_withNonExistingId_returns404() throws Exception {
        when(themeService.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/themes/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCss_returnsCssContent() throws Exception {
        var theme = new Theme();
        theme.setId("classic");
        theme.setCssContent("body { color: black; }");

        when(themeService.findById("classic")).thenReturn(Optional.of(theme));

        mockMvc.perform(get("/api/themes/classic/css"))
                .andExpect(status().isOk())
                .andExpect(content().string("body { color: black; }"))
                .andExpect(header().string("Content-Type", "text/css; charset=utf-8"));
    }

    @Test
    void getCss_withNonExistingId_returns404() throws Exception {
        when(themeService.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/themes/missing/css"))
                .andExpect(status().isNotFound());
    }
}
