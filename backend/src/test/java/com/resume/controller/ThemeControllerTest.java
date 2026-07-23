package com.resume.controller;

import com.resume.config.JwtUtil;
import com.resume.dto.VariableDeclaration;
import com.resume.entity.Theme;
import com.resume.service.ThemeService;
import com.resume.dto.ThemeDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ThemeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId + ":testuser", null, Collections.emptyList()));
    }

    @Test
    void list_returnsThemes() throws Exception {
        var theme = new Theme();
        theme.setId("classic");
        theme.setName("Classic");
        theme.setDescription("Desc");

        when(themeService.findAll((Long) null)).thenReturn(List.of(theme));

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
    void get_withExistingId_returnsThemeWithVariablesSchema() throws Exception {
        var theme = new Theme();
        theme.setId("modern");
        theme.setName("Modern");
        theme.setVariablesSchema("[{\"name\":\"--primary-color\",\"type\":\"color\",\"defaultValue\":\"#2563eb\"}]");

        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        mockMvc.perform(get("/api/themes/modern"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variablesSchema").isNotEmpty());
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

    @Test
    void getVariables_withExistingId_returnsVariableDeclarations() throws Exception {
        var var1 = new VariableDeclaration();
        var1.setName("--primary-color");
        var1.setType("color");
        var1.setDefaultValue("#2563eb");
        var1.setLabel("Primary Color");
        var1.setGroup("Colors");

        when(themeService.getVariables("modern")).thenReturn(List.of(var1));

        mockMvc.perform(get("/api/themes/modern/variables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("--primary-color"))
                .andExpect(jsonPath("$[0].type").value("color"))
                .andExpect(jsonPath("$[0].defaultValue").value("#2563eb"))
                .andExpect(jsonPath("$[0].label").value("Primary Color"))
                .andExpect(jsonPath("$[0].group").value("Colors"));
    }

    @Test
    void getVariables_withNonExistingId_returns404() throws Exception {
        when(themeService.getVariables("missing")).thenReturn(null);

        mockMvc.perform(get("/api/themes/missing/variables"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTheme_returns201() throws Exception {
        authenticateAs(1L);
        Theme created = new Theme();
        created.setId("user-1-abc12345");
        created.setName("My Theme");
        created.setBuiltIn(false);

        when(themeService.createCustomTheme(any(ThemeDTO.class), eq(1L))).thenReturn(created);

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"My Theme\",\"layout\":\"single\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("user-1-abc12345"))
                .andExpect(jsonPath("$.name").value("My Theme"));
    }

    @Test
    void createTheme_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"My Theme\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTheme_withEmptyName_returns400() throws Exception {
        authenticateAs(1L);
        when(themeService.createCustomTheme(any(ThemeDTO.class), eq(1L)))
                .thenThrow(new IllegalArgumentException("Theme name is required"));

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTheme_returns200() throws Exception {
        authenticateAs(1L);
        Theme updated = new Theme();
        updated.setId("user-1-abc12345");
        updated.setName("Updated Theme");

        when(themeService.updateCustom(eq("user-1-abc12345"), any(ThemeDTO.class), eq(1L)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/themes/user-1-abc12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Theme\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Theme"));
    }

    @Test
    void updateTheme_notOwner_returns403() throws Exception {
        authenticateAs(1L);
        when(themeService.updateCustom(eq("user-2-xxx"), any(ThemeDTO.class), eq(1L)))
                .thenThrow(new SecurityException("Not authorized"));

        mockMvc.perform(put("/api/themes/user-2-xxx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTheme_builtIn_succeeds() throws Exception {
        authenticateAs(1L);
        Theme updated = new Theme();
        updated.setId("classic");
        updated.setName("Updated Classic");
        updated.setBuiltIn(true);
        when(themeService.updateCustom(eq("classic"), any(ThemeDTO.class), eq(1L)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/themes/classic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCustomTheme_returns204() throws Exception {
        authenticateAs(1L);
        doNothing().when(themeService).deleteCustom("user-1-abc12345", 1L);

        mockMvc.perform(delete("/api/themes/user-1-abc12345"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBuiltInTheme_returns409() throws Exception {
        authenticateAs(1L);
        doThrow(new IllegalStateException("Cannot delete a built-in theme"))
                .when(themeService).deleteCustom("classic", 1L);

        mockMvc.perform(delete("/api/themes/classic"))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteTheme_notOwner_returns403() throws Exception {
        authenticateAs(1L);
        doThrow(new SecurityException("Not authorized"))
                .when(themeService).deleteCustom("user-2-xxx", 1L);

        mockMvc.perform(delete("/api/themes/user-2-xxx"))
                .andExpect(status().isForbidden());
    }
}
