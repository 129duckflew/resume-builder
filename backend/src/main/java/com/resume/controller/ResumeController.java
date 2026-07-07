package com.resume.controller;

import com.resume.dto.ResumeDTO;
import com.resume.entity.Resume;
import com.resume.service.ExportService;
import com.resume.service.ResumeService;
import com.resume.service.SmartOnePageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final ExportService exportService;
    private final SmartOnePageService smartOnePageService;

    public ResumeController(ResumeService resumeService, ExportService exportService,
                            SmartOnePageService smartOnePageService) {
        this.resumeService = resumeService;
        this.exportService = exportService;
        this.smartOnePageService = smartOnePageService;
    }

    @GetMapping
    public List<Resume> list() {
        return resumeService.findAll();
    }

    @PostMapping
    public Resume create(@Valid @RequestBody ResumeDTO dto) {
        return resumeService.create(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> get(@PathVariable String id) {
        return resumeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Resume update(@PathVariable String id, @Valid @RequestBody ResumeDTO dto) {
        return resumeService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        resumeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/preview")
    public ResponseEntity<String> preview(@PathVariable String id) {
        Resume resume = resumeService.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        String html = exportService.generateHtml(resume);
        return ResponseEntity.ok(html);
    }

    @PostMapping("/{id}/export/html")
    public ResponseEntity<String> exportHtml(@PathVariable String id) {
        Resume resume = resumeService.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        String html = exportService.generateHtml(resume);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"resume.html\"")
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @PostMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable String id,
                                            @RequestParam(defaultValue = "true") boolean smartOnePage) {
        Resume resume = resumeService.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        String html = exportService.generateHtml(resume);

        if (smartOnePage) {
            SmartOnePageService.AdjustmentResult adjustment =
                    smartOnePageService.calculateOptimalSettings(resume, html);
            if (!adjustment.fitsOnOnePage) {
                return ResponseEntity.badRequest().body(
                        adjustment.warning.getBytes()
                );
            }
        }

        // Placeholder for Playwright-based PDF generation
        byte[] pdfBytes = new byte[0];

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"resume.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
