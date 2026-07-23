package com.resume.controller;

import com.resume.config.JwtUtil;
import com.resume.dto.VersionDiffResponse;
import com.resume.entity.Resume;
import com.resume.service.ResumeService;
import com.resume.service.ResumeVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeVersionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResumeVersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumeVersionService versionService;

    @MockitoBean
    private ResumeService resumeService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId + ":testuser", null, List.of()));
    }

    @Test
    void diff_authenticated_200() throws Exception {
        Resume resume = new Resume();
        resume.setId("r1");
        when(resumeService.findByIdAndUserId("r1", userId)).thenReturn(Optional.of(resume));

        VersionDiffResponse diffResponse = new VersionDiffResponse();
        when(versionService.getDiff("r1", 1, 2)).thenReturn(diffResponse);

        mockMvc.perform(get("/api/resumes/r1/versions/diff?a=1&b=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versionA").doesNotExist());
    }

    @Test
    void diff_notOwner_404() throws Exception {
        when(resumeService.findByIdAndUserId("r1", userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/resumes/r1/versions/diff?a=1&b=2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void diff_sameVersions_400() throws Exception {
        Resume resume = new Resume();
        resume.setId("r1");
        when(resumeService.findByIdAndUserId("r1", userId)).thenReturn(Optional.of(resume));

        mockMvc.perform(get("/api/resumes/r1/versions/diff?a=2&b=2"))
                .andExpect(status().isBadRequest());
    }
}
