package com.resume.controller;

import com.resume.dto.DesensitizeRuleDTO;
import com.resume.service.DesensitizeService;
import com.resume.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DesensitizeController.class)
@AutoConfigureMockMvc(addFilters = false)
class DesensitizeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DesensitizeService desensitizeService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void getRules_returnsRules() throws Exception {
        var dto = new DesensitizeRuleDTO();
        dto.setName("phone");
        dto.setPattern("pattern");
        dto.setReplacement("repl");
        dto.setEnabled(true);
        dto.setDefaultRule(true);
        dto.setSortOrder(1);

        when(desensitizeService.getEffectiveRules(1L)).thenReturn(List.of(dto));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1:testuser", null, List.of()));

        mockMvc.perform(get("/api/users/desensitize-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("phone"))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[0].defaultRule").value(true));
    }

    @Test
    void saveRules_savesAndReturnsOk() throws Exception {
        var dto = new DesensitizeRuleDTO();
        dto.setName("phone");
        dto.setPattern("pattern");
        dto.setReplacement("repl");
        dto.setEnabled(true);
        dto.setSortOrder(1);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1:testuser", null, List.of()));

        mockMvc.perform(put("/api/users/desensitize-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(dto))))
                .andExpect(status().isOk());

        verify(desensitizeService).saveUserRules(eq(1L), any());
    }
}
