package com.resume.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    private final Browser browser;
    private final boolean available;

    public PdfGenerationService(Browser browser) {
        this.browser = browser;
        this.available = browser != null;
    }

    public byte[] generatePdf(String htmlContent) {
        if (!available) {
            throw new IllegalStateException(
                    "PDF generation is not available. Playwright/Chromium not installed.");
        }

        long start = System.currentTimeMillis();
        try (BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(794, 1123)
                        .setDeviceScaleFactor(1.0)
        )) {
            Page page = context.newPage();
            page.setContent(htmlContent);
            page.waitForLoadState();

            Page.PdfOptions pdfOptions = new Page.PdfOptions()
                    .setFormat("A4")
                    .setPrintBackground(true)
                    .setPreferCSSPageSize(true);

            byte[] pdf = page.pdf(pdfOptions);

            log.info("PDF generated in {}ms ({} bytes)",
                    System.currentTimeMillis() - start, pdf.length);
            return pdf;

        } catch (Exception e) {
            log.error("PDF generation failed", e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    public boolean isAvailable() {
        return available;
    }
}
