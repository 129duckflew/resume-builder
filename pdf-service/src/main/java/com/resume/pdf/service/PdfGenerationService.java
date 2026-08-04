package com.resume.pdf.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    static final int A4_WIDTH_PX = 794;
    static final int A4_HEIGHT_PX = 1123;

    static final String FOOTER_TEMPLATE = """
            <div style="font-size:8px; width:100%; text-align:center; color:#666666;
                        font-family:Helvetica, Arial, sans-serif;">
            第 <span class="pageNumber"></span> 页 / 共 <span class="totalPages"></span> 页
            </div>""";

    private final Browser browser;

    public PdfGenerationService(Optional<Browser> browser) {
        this.browser = browser.orElse(null);
    }

    public boolean isAvailable() {
        return browser != null && browser.isConnected();
    }

    public byte[] generatePdf(String htmlContent) {
        requireAvailable();
        long start = System.currentTimeMillis();
        try (BrowserContext context = newRenderContext()) {
            Page page = context.newPage();
            page.setContent(htmlContent);
            page.waitForLoadState();

            Page.PdfOptions pdfOptions = new Page.PdfOptions()
                    .setFormat("A4")
                    .setPrintBackground(true)
                    .setPreferCSSPageSize(true)
                    .setDisplayHeaderFooter(true)
                    .setHeaderTemplate("<div></div>")
                    .setFooterTemplate(FOOTER_TEMPLATE);

            byte[] pdf = page.pdf(pdfOptions);
            log.info("PDF generated in {}ms ({} bytes)",
                    System.currentTimeMillis() - start, pdf.length);
            return pdf;
        } catch (Exception e) {
            log.error("PDF generation failed", e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    public double measureHeight(String htmlContent) {
        requireAvailable();
        try (BrowserContext context = newRenderContext()) {
            Page page = context.newPage();
            page.setContent(htmlContent);
            page.waitForLoadState();
            return ((Number) page.evaluate("document.body.scrollHeight")).doubleValue();
        } catch (Exception e) {
            log.error("Height measurement failed", e);
            throw new RuntimeException("Failed to measure height: " + e.getMessage(), e);
        }
    }

    private BrowserContext newRenderContext() {
        return browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(A4_WIDTH_PX, A4_HEIGHT_PX)
                .setDeviceScaleFactor(1.0));
    }

    private void requireAvailable() {
        if (!isAvailable()) {
            throw new IllegalStateException("Chromium browser is not available.");
        }
    }
}
