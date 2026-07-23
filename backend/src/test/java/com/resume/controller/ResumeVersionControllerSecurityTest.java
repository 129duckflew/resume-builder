package com.resume.controller;

import com.resume.config.JwtUtil;
import com.resume.service.ResumeService;
import com.resume.service.ResumeVersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResumeVersionController.class)
class ResumeVersionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumeVersionService versionService;

    @MockitoBean
    private ResumeService resumeService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void diff_unauthenticated_401() throws Exception {
        mockMvc.perform(get("/api/resumes/r1/versions/diff?a=1&b=2"))
                .andExpect(status().isUnauthorized());
    }
}
