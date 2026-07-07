package com.resume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.ResumeDTO;
import com.resume.entity.Resume;
import com.resume.service.ExportService;
import com.resume.service.PdfGenerationService;
import com.resume.service.ResumeService;
import com.resume.service.SmartOnePageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeController.class)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeService resumeService;

    @MockBean
    private ExportService exportService;

    @MockBean
    private SmartOnePageService smartOnePageService;

    @MockBean
    private PdfGenerationService pdfGenerationService;

    @Test
    void list_returnsResumes() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        resume.setTitle("Test");

        when(resumeService.findAll()).thenReturn(List.of(resume));

        mockMvc.perform(get("/api/resumes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].title").value("Test"));
    }

    @Test
    void get_withExistingId_returnsResume() throws Exception {
        var resume = new Resume();
        resume.setId("abc");
        resume.setTitle("Found");

        when(resumeService.findById("abc")).thenReturn(Optional.of(resume));

        mockMvc.perform(get("/api/resumes/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Found"));
    }

    @Test
    void get_withNonExistingId_returns404() throws Exception {
        when(resumeService.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/resumes/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_createsAndReturnsResume() throws Exception {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle("New");

        var saved = new Resume();
        saved.setId("new-id");
        saved.setTitle("New");

        when(resumeService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/api/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("new-id"))
                .andExpect(jsonPath("$.title").value("New"));
    }

    @Test
    void create_withBlankTitle_returns400() throws Exception {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle("");

        mockMvc.perform(post("/api/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_updatesResume() throws Exception {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle("Updated");
        dto.setContent("# New");

        var updated = new Resume();
        updated.setId("1");
        updated.setTitle("Updated");
        updated.setContent("# New");

        when(resumeService.update(eq("1"), any())).thenReturn(updated);

        mockMvc.perform(put("/api/resumes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.content").value("# New"));
    }

    @Test
    void update_withPartialData_returns200() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        resume.setTitle("Original");
        resume.setContent("# Hello");
        resume.setThemeId("classic");

        when(resumeService.findById("1")).thenReturn(Optional.of(resume));

        // Only send themeId — no title or content
        mockMvc.perform(put("/api/resumes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"themeId\":\"modern\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/resumes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void preview_returnsHtml() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        resume.setContent("# Hello");

        when(resumeService.findById("1")).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume)).thenReturn("<h1>Hello</h1>");

        mockMvc.perform(post("/api/resumes/1/preview"))
                .andExpect(status().isOk())
                .andExpect(content().string("<h1>Hello</h1>"));
    }

    @Test
    void exportHtml_returnsAttachment() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        resume.setContent("# Hello");

        when(resumeService.findById("1")).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume)).thenReturn("<h1>Hello</h1>");

        mockMvc.perform(post("/api/resumes/1/export/html"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"resume.html\""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    void exportPdf_whenSmartOnePageFails_returns400() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        resume.setContent("# Long");

        when(resumeService.findById("1")).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume)).thenReturn("<h1>Long</h1>");

        var adjustment = new SmartOnePageService.AdjustmentResult();
        adjustment.fitsOnOnePage = false;
        adjustment.warning = "Too long";
        when(smartOnePageService.calculateOptimalSettings(any(), anyString()))
                .thenReturn(adjustment);

        mockMvc.perform(post("/api/resumes/1/export/pdf?smartOnePage=true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Too long"));
    }

    @Test
    void exportPdf_whenPdfServiceUnavailable_returns503() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        resume.setContent("# Hello");

        when(resumeService.findById("1")).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume)).thenReturn("<h1>Hello</h1>");

        var adjustment = new SmartOnePageService.AdjustmentResult();
        adjustment.fitsOnOnePage = true;
        when(smartOnePageService.calculateOptimalSettings(any(), anyString()))
                .thenReturn(adjustment);
        when(pdfGenerationService.isAvailable()).thenReturn(false);

        mockMvc.perform(post("/api/resumes/1/export/pdf"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void exportPdf_returnsPdfAttachment() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        resume.setContent("# Hello");

        when(resumeService.findById("1")).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume)).thenReturn("<h1>Hello</h1>");

        var adjustment = new SmartOnePageService.AdjustmentResult();
        adjustment.fitsOnOnePage = true;
        when(smartOnePageService.calculateOptimalSettings(any(), anyString()))
                .thenReturn(adjustment);
        when(pdfGenerationService.isAvailable()).thenReturn(true);
        when(pdfGenerationService.generatePdf(anyString()))
                .thenReturn("PDF DATA".getBytes());

        mockMvc.perform(post("/api/resumes/1/export/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"resume.pdf\""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
    }
}
