package com.resume.controller;

import com.resume.dto.JsonResumeDTO;
import com.resume.dto.ResumeDTO;
import com.resume.entity.Resume;
import com.resume.service.ExportService;
import com.resume.service.JsonResumeConverter;
import com.resume.service.PdfGenerationService;
import com.resume.service.ResumeService;
import com.resume.service.SmartOnePageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);

    private final ResumeService resumeService;
    private final ExportService exportService;
    private final SmartOnePageService smartOnePageService;
    private final PdfGenerationService pdfGenerationService;
    private final JsonResumeConverter jsonResumeConverter;

    public ResumeController(ResumeService resumeService, ExportService exportService,
                            SmartOnePageService smartOnePageService,
                            PdfGenerationService pdfGenerationService,
                            JsonResumeConverter jsonResumeConverter) {
        this.resumeService = resumeService;
        this.exportService = exportService;
        this.smartOnePageService = smartOnePageService;
        this.pdfGenerationService = pdfGenerationService;
        this.jsonResumeConverter = jsonResumeConverter;
    }

    @GetMapping
    public List<Resume> list() {
        return resumeService.findByUserId(currentUserId());
    }

    @PostMapping
    public Resume create(@Valid @RequestBody ResumeDTO dto) {
        return resumeService.create(dto, currentUserId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> get(@PathVariable String id) {
        return resumeService.findByIdAndUserId(id, currentUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Resume update(@PathVariable String id, @RequestBody ResumeDTO dto) {
        return resumeService.update(id, dto, currentUserId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        resumeService.delete(id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/preview")
    public ResponseEntity<?> preview(@PathVariable String id,
                                     @RequestParam(defaultValue = "false") boolean smartOnePage,
                                     @RequestParam(defaultValue = "false") boolean desensitize) {
        Long userId = currentUserId();
        Resume resume = resumeService.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        String html = exportService.generateHtml(resume, desensitize, userId);

        if (smartOnePage) {
            try {
                SmartOnePageService.AdjustmentResult adjustment =
                        smartOnePageService.calculateOptimalSettings(resume, html);
                html = SmartOnePageService.injectCssVariables(html, adjustment);
            } catch (RuntimeException e) {
                log.warn("Smart one-page adjustment failed, using default layout", e);
            }
        }

        return ResponseEntity.ok(html);
    }

    @PostMapping("/{id}/export/html")
    public ResponseEntity<?> exportHtml(@PathVariable String id,
                                        @RequestParam(defaultValue = "false") boolean smartOnePage,
                                        @RequestParam(defaultValue = "false") boolean desensitize) {
        Long userId = currentUserId();
        Resume resume = resumeService.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        String html = exportService.generateHtml(resume, desensitize, userId);

        if (smartOnePage) {
            try {
                SmartOnePageService.AdjustmentResult adjustment =
                        smartOnePageService.calculateOptimalSettings(resume, html);
                html = SmartOnePageService.injectCssVariables(html, adjustment);
            } catch (RuntimeException e) {
                log.warn("Smart one-page adjustment failed, using default layout", e);
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"resume.html\"")
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @PostMapping("/{id}/export/pdf")
    public ResponseEntity<?> exportPdf(@PathVariable String id,
                                       @RequestParam(defaultValue = "true") boolean smartOnePage,
                                       @RequestParam(defaultValue = "false") boolean desensitize) {
        Long userId = currentUserId();
        Resume resume = resumeService.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        String html = exportService.generateHtml(resume, desensitize, userId);

        if (smartOnePage) {
            SmartOnePageService.AdjustmentResult adjustment =
                    smartOnePageService.calculateOptimalSettings(resume, html);
            if (!adjustment.fitsOnOnePage) {
                return ResponseEntity.badRequest().body(
                        java.util.Map.of("error", adjustment.warning));
            }
            html = SmartOnePageService.injectCssVariables(html, adjustment);
        }

        if (!pdfGenerationService.isAvailable()) {
            return ResponseEntity.status(503)
                    .body(java.util.Map.of("error",
                            "PDF generation is not available. Playwright/Chromium not installed."));
        }

        try {
            byte[] pdfBytes = pdfGenerationService.generatePdf(html);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"resume.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(java.util.Map.of("error", "PDF generation failed: " + e.getMessage()));
        }
    }

    @PostMapping("/import/json")
    public Resume importJson(@RequestBody JsonResumeDTO jsonResume) {
        String markdown = jsonResumeConverter.toMarkdown(jsonResume);
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle(jsonResume.getBasics() != null && jsonResume.getBasics().getName() != null
                ? jsonResume.getBasics().getName() : "Imported Resume");
        dto.setContent(markdown);
        return resumeService.create(dto, currentUserId());
    }

    @GetMapping("/{id}/export/json")
    public ResponseEntity<JsonResumeDTO> exportJson(@PathVariable String id) {
        Resume resume = resumeService.findByIdAndUserId(id, currentUserId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        return ResponseEntity.ok(jsonResumeConverter.fromResume(resume));
    }

    private Long currentUserId() {
        String principal = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return Long.parseLong(principal.split(":", 2)[0]);
    }
}
