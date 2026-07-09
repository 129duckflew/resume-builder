package com.resume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.ResumeDTO;
import com.resume.dto.ResumeStyleDTO;
import com.resume.entity.Resume;
import com.resume.entity.ResumeStyle;
import com.resume.service.ExportService;
import com.resume.config.JwtUtil;
import com.resume.service.PdfGenerationService;
import com.resume.service.ResumeService;
import com.resume.service.JsonResumeConverter;
import com.resume.service.ResumeStyleService;
import com.resume.service.SmartOnePageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeController.class)
@AutoConfigureMockMvc(addFilters = false)
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

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JsonResumeConverter jsonResumeConverter;

    @MockBean
    private ResumeStyleService resumeStyleService;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId + ":testuser", null, List.of()));
    }

    @Test
    void list_returnsResumes() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        resume.setTitle("Test");

        when(resumeService.findByUserId(userId)).thenReturn(List.of(resume));

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

        when(resumeService.findByIdAndUserId("abc", userId)).thenReturn(Optional.of(resume));

        mockMvc.perform(get("/api/resumes/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Found"));
    }

    @Test
    void get_withNonExistingId_returns404() throws Exception {
        when(resumeService.findByIdAndUserId("missing", userId)).thenReturn(Optional.empty());

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

        when(resumeService.create(any(), eq(userId))).thenReturn(saved);

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

        when(resumeService.update(eq("1"), any(), eq(userId))).thenReturn(updated);

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

        when(resumeService.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));

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

        when(resumeService.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume, false, userId)).thenReturn("<h1>Hello</h1>");

        mockMvc.perform(post("/api/resumes/1/preview"))
                .andExpect(status().isOk())
                .andExpect(content().string("<h1>Hello</h1>"));
    }

    @Test
    void preview_withSmartOnePage_appliesAdjustments() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        resume.setContent("# Hello");

        when(resumeService.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume, false, userId)).thenReturn("<h1>Hello</h1>");

        var adjustment = new SmartOnePageService.AdjustmentResult();
        adjustment.fontSize = 9f;
        adjustment.lineHeight = 1.2f;
        when(smartOnePageService.calculateOptimalSettings(any(), anyString()))
                .thenReturn(adjustment);

        mockMvc.perform(post("/api/resumes/1/preview?smartOnePage=true"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("--resume-font-size: 9.0pt")));
    }

    @Test
    void exportHtml_returnsAttachment() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        resume.setContent("# Hello");

        when(resumeService.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume, false, userId)).thenReturn("<h1>Hello</h1>");

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

        when(resumeService.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume, false, userId)).thenReturn("<h1>Long</h1>");

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

        when(resumeService.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume, false, userId)).thenReturn("<h1>Hello</h1>");

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

        when(resumeService.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume, false, userId)).thenReturn("<h1>Hello</h1>");

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

    @Test
    void exportPdf_withSmartOnePageFalse_skipsSmartLogic() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        resume.setContent("# Hello");

        when(resumeService.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));
        when(exportService.generateHtml(resume, false, userId)).thenReturn("<h1>Hello</h1>");
        when(pdfGenerationService.isAvailable()).thenReturn(true);
        when(pdfGenerationService.generatePdf(anyString()))
                .thenReturn("PDF DATA".getBytes());

        mockMvc.perform(post("/api/resumes/1/export/pdf?smartOnePage=false"))
                .andExpect(status().isOk());

        verify(smartOnePageService, never()).calculateOptimalSettings(any(), anyString());
    }

    @Test
    void importJson_createsResume() throws Exception {
        String json = """
                {"basics":{"name":"Alice","email":"a@b.com"}}
                """;
        when(jsonResumeConverter.toMarkdown(any())).thenReturn("# Personal Info\n\nName: Alice");
        when(resumeService.create(any(), eq(1L))).thenAnswer(i -> {
            Resume r = new Resume();
            r.setId("new-id");
            r.setTitle("Alice");
            return r;
        });

        mockMvc.perform(post("/api/resumes/import/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("new-id"));
    }

    @Test
    void exportJson_returnsJson() throws Exception {
        Resume resume = new Resume();
        resume.setId("1");
        resume.setContent("# Personal Info\n\nName: Alice\n");

        when(resumeService.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));
        when(jsonResumeConverter.fromResume(resume)).thenReturn(new com.resume.dto.JsonResumeDTO());

        mockMvc.perform(get("/api/resumes/1/export/json"))
                .andExpect(status().isOk());
    }

    @Test
    void getStyle_returnsStyleWithCustomVariables() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        var style = new ResumeStyle();
        style.setResumeId("1");
        style.setThemeId("classic");
        style.setCustomVariables("{\"--primary\":\"#000\"}");

        when(resumeService.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));
        when(resumeStyleService.getStyle("1", "classic")).thenReturn(Optional.of(style));

        mockMvc.perform(get("/api/resumes/1/styles?themeId=classic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customVariables").isNotEmpty());
    }

    @Test
    void saveStyle_acceptsDtoWithCustomVariables() throws Exception {
        var resume = new Resume();
        resume.setId("1");
        var saved = new ResumeStyle();
        saved.setResumeId("1");
        saved.setThemeId("modern");
        saved.setCustomVariables("{\"--color\":\"#ff0\"}");

        when(resumeService.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));
        when(resumeStyleService.saveStyle(eq("1"), eq("modern"), any(ResumeStyleDTO.class)))
                .thenReturn(saved);

        mockMvc.perform(put("/api/resumes/1/styles?themeId=modern")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fontSize\":12,\"customVariables\":{\"--color\":\"#ff0\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customVariables").isNotEmpty());
    }
}
