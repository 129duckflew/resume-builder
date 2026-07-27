package com.resume.controller;

import com.resume.dto.JsonResumeDTO;
import com.resume.dto.PreviewRequest;
import com.resume.dto.ResumeDTO;
import com.resume.dto.ResumeStyleDTO;
import com.resume.entity.Resume;
import com.resume.entity.ResumeStyle;
import com.resume.service.ExportService;
import com.resume.service.JsonResumeConverter;
import com.resume.service.PdfServiceClient;
import com.resume.service.ResumeService;
import com.resume.service.ResumeStyleService;
import com.resume.service.SmartOnePageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.resume.config.CurrentUserId;
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
    private final PdfServiceClient pdfServiceClient;
    private final JsonResumeConverter jsonResumeConverter;
    private final ResumeStyleService resumeStyleService;

    public ResumeController(ResumeService resumeService, ExportService exportService,
                            SmartOnePageService smartOnePageService,
                            PdfServiceClient pdfServiceClient,
                            JsonResumeConverter jsonResumeConverter,
                            ResumeStyleService resumeStyleService) {
        this.resumeService = resumeService;
        this.exportService = exportService;
        this.smartOnePageService = smartOnePageService;
        this.pdfServiceClient = pdfServiceClient;
        this.jsonResumeConverter = jsonResumeConverter;
        this.resumeStyleService = resumeStyleService;
    }

    @GetMapping
    public List<Resume> list(@CurrentUserId Long userId) {
        return resumeService.findByUserId(userId);
    }

    @PostMapping
    public Resume create(@Valid @RequestBody ResumeDTO dto, @CurrentUserId Long userId) {
        return resumeService.create(dto, userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> get(@PathVariable String id, @CurrentUserId Long userId) {
        return resumeService.findByIdAndUserId(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Resume update(@PathVariable String id, @RequestBody ResumeDTO dto, @CurrentUserId Long userId) {
        return resumeService.update(id, dto, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @CurrentUserId Long userId) {
        resumeService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/preview")
    public ResponseEntity<?> preview(@PathVariable String id,
                                     @RequestParam(defaultValue = "false") boolean smartOnePage,
                                     @RequestParam(defaultValue = "false") boolean desensitize,
                                     @RequestBody(required = false) PreviewRequest body,
                                     @CurrentUserId Long userId) {
        Resume resume = resumeService.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        if (body != null && body.getContent() != null) {
            resume.setContent(body.getContent());
        }
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
                                        @RequestParam(defaultValue = "false") boolean desensitize,
                                        @CurrentUserId Long userId) {
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
                                       @RequestParam(defaultValue = "false") boolean desensitize,
                                       @CurrentUserId Long userId) {
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

        if (!pdfServiceClient.isAvailable()) {
            return ResponseEntity.status(503)
                    .body(java.util.Map.of("error",
                            "PDF service is unavailable. Please try again later."));
        }

        try {
            byte[] pdfBytes = pdfServiceClient.generatePdf(html);
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
    public Resume importJson(@RequestBody JsonResumeDTO jsonResume, @CurrentUserId Long userId) {
        String markdown = jsonResumeConverter.toMarkdown(jsonResume);
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle(jsonResume.getBasics() != null && jsonResume.getBasics().getName() != null
                ? jsonResume.getBasics().getName() : "Imported Resume");
        dto.setContent(markdown);
        return resumeService.create(dto, userId);
    }

    @GetMapping("/{id}/export/json")
    public ResponseEntity<JsonResumeDTO> exportJson(@PathVariable String id, @CurrentUserId Long userId) {
        Resume resume = resumeService.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        return ResponseEntity.ok(jsonResumeConverter.fromResume(resume));
    }

    @GetMapping("/{id}/styles")
    public ResponseEntity<ResumeStyle> getStyle(@PathVariable String id,
                                                 @RequestParam String themeId,
                                                 @CurrentUserId Long userId) {
        resumeService.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        return resumeStyleService.getStyle(id, themeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{id}/styles")
    public ResumeStyle saveStyle(@PathVariable String id,
                                 @RequestParam String themeId,
                                 @RequestBody ResumeStyleDTO dto,
                                 @CurrentUserId Long userId) {
        resumeService.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        return resumeStyleService.saveStyle(id, themeId, dto);
    }
}
