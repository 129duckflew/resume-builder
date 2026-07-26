package com.resume.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Facade over the internal pdf-service. Keeps the historical
 * isAvailable()/generatePdf() contract used by ResumeController.
 */
@Service
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    private final PdfServiceClient pdfServiceClient;

    public PdfGenerationService(PdfServiceClient pdfServiceClient) {
        this.pdfServiceClient = pdfServiceClient;
    }

    public boolean isAvailable() {
        return pdfServiceClient.isAvailable();
    }

    public byte[] generatePdf(String htmlContent) {
        try {
            return pdfServiceClient.generatePdf(htmlContent);
        } catch (Exception e) {
            log.error("PDF generation via pdf-service failed", e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
}
