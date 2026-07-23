package com.resume.controller;

import com.resume.entity.SectionTemplate;
import com.resume.service.SectionTemplateService;
import com.resume.config.JwtUtil;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SectionTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class SectionTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SectionTemplateService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1:testuser", null, List.of()));
    }

    @Test
    void list_returnsTemplates() throws Exception {
        SectionTemplate t = new SectionTemplate();
        t.setId(1L);
        t.setName("Personal Info");

        when(service.getEffectiveTemplates(1L)).thenReturn(List.of(t));

        mockMvc.perform(get("/api/section-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Personal Info"));
    }

    @Test
    void create_createsAndReturns() throws Exception {
        SectionTemplate t = new SectionTemplate();
        t.setId(1L);
        t.setName("Custom");
        t.setPrompt("# Custom");

        when(service.create(any(), eq(1L))).thenReturn(t);

        mockMvc.perform(post("/api/section-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(t)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/section-templates/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L, 1L);
    }
}
