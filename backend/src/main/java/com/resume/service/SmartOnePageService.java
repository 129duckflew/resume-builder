package com.resume.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.resume.entity.Resume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmartOnePageService {

    private static final Logger log = LoggerFactory.getLogger(SmartOnePageService.class);

    private static final int A4_WIDTH_PX = 794;
    private static final int A4_HEIGHT_PX = 1123;
    private static final float MIN_FONT_SIZE = 8f;
    private static final float MIN_LINE_HEIGHT = 1.2f;
    private static final float MIN_SECTION_MARGIN = 4f;
    private static final float INITIAL_FONT_SIZE = 11f;
    private static final float INITIAL_LINE_HEIGHT = 1.4f;
    private static final float INITIAL_SECTION_MARGIN = 16f;
    private static final int MAX_ITERATIONS = 20;

    private final Browser browser;
    private final boolean available;

    public SmartOnePageService(java.util.Optional<Browser> browser) {
        this.browser = browser.orElse(null);
        this.available = this.browser != null;
    }

    public static class AdjustmentResult {
        public float fontSize = INITIAL_FONT_SIZE;
        public float lineHeight = INITIAL_LINE_HEIGHT;
        public float sectionMargin = INITIAL_SECTION_MARGIN;
        public boolean fitsOnOnePage = true;
        public String warning;
    }

    public AdjustmentResult calculateOptimalSettings(Resume resume, String htmlContent) {
        AdjustmentResult result = new AdjustmentResult();

        if (resume.getFontSize() != null && resume.getFontSize() > 0) {
            result.fontSize = resume.getFontSize();
        }
        if (resume.getLineHeight() != null && resume.getLineHeight() > 0) {
            result.lineHeight = resume.getLineHeight();
        }

        if (!available) {
            log.warn("Playwright not available, using estimation fallback");
            estimateFromContent(htmlContent, result);
            return result;
        }

        measureAndAdjust(htmlContent, result);
        return result;
    }

    private void measureAndAdjust(String html, AdjustmentResult result) {
        try (BrowserContext ctx = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(A4_WIDTH_PX, A4_HEIGHT_PX)
                        .setDeviceScaleFactor(1.0)
        )) {
            Page page = ctx.newPage();

            for (int i = 0; i < MAX_ITERATIONS; i++) {
                String styledHtml = injectCssVariables(html, result);
                page.setContent(styledHtml);
                page.waitForLoadState();

                double scrollHeight = ((Number) page.evaluate(
                        "document.body.scrollHeight")).doubleValue();

                if (scrollHeight <= A4_HEIGHT_PX + 5) {
                    log.info("Content fits after {} adjustments (fontSize={}, lineHeight={})",
                            i, result.fontSize, result.lineHeight);
                    return;
                }

                boolean adjusted = false;

                if (result.fontSize > MIN_FONT_SIZE) {
                    result.fontSize = Math.max(MIN_FONT_SIZE, result.fontSize - 0.5f);
                    adjusted = true;
                }
                if (result.lineHeight > MIN_LINE_HEIGHT && !adjusted) {
                    result.lineHeight = Math.max(MIN_LINE_HEIGHT, result.lineHeight - 0.05f);
                    adjusted = true;
                }
                if (result.sectionMargin > MIN_SECTION_MARGIN && !adjusted) {
                    result.sectionMargin = Math.max(MIN_SECTION_MARGIN, result.sectionMargin - 2f);
                    adjusted = true;
                }

                if (!adjusted) {
                    result.fitsOnOnePage = false;
                    result.warning = "Content too long for one page. " +
                            "Consider trimming or reducing font size manually.";
                    log.warn("Content still exceeds one page after max compression");
                    return;
                }
            }

            result.fitsOnOnePage = false;
            result.warning = "Could not fit content to one page after " +
                    MAX_ITERATIONS + " attempts. Try trimming your content.";
        }
    }

    public static String injectCssVariables(String html, AdjustmentResult result) {
        if (html == null || result == null) return html;

        float h1Size = Math.max(result.fontSize * 2.1f, 14f);
        float h2Size = Math.max(result.fontSize * 1.1f, 9f);
        float h3Size = Math.max(result.fontSize * 1.0f, 8.5f);
        float liMarginBottom = Math.max(result.fontSize * 0.25f, 1f);
        float pMarginBottom = Math.max(result.fontSize * 0.4f, 2f);

        String vars = String.format("""
                <style>
                :root {
                    --resume-font-size: %.1fpt;
                    --resume-line-height: %.2f;
                    --resume-section-margin: %.0fpx;
                    --resume-h1-size: %.1fpt;
                    --resume-h2-size: %.1fpt;
                    --resume-h3-size: %.1fpt;
                }
                .resume-page { font-size: var(--resume-font-size); line-height: var(--resume-line-height); }
                .resume-page h1 { font-size: var(--resume-h1-size, %.1fpt); }
                .resume-page h2 { font-size: var(--resume-h2-size, %.1fpt); }
                .resume-page h3 { font-size: var(--resume-h3-size, %.1fpt); }
                .resume-page p { font-size: var(--resume-font-size); margin-bottom: %.0fpx; }
                .resume-page li { font-size: var(--resume-font-size); margin-bottom: %.0fpx; }
                .resume-section { margin-bottom: var(--resume-section-margin); }
                </style>
                """,
                result.fontSize, result.lineHeight, result.sectionMargin,
                h1Size, h2Size, h3Size,
                h1Size, h2Size, h3Size,
                pMarginBottom, liMarginBottom);

        int headEnd = html.indexOf("</head>");
        if (headEnd > 0) {
            return html.substring(0, headEnd) + vars + html.substring(headEnd);
        }
        return vars + html;
    }

    private void estimateFromContent(String html, AdjustmentResult result) {
        int textLength = html.replaceAll("<[^>]*>", "").length();
        int lineCount = textLength / 80;

        if (lineCount > 60) {
            float reduction = Math.min((lineCount - 60) / 60f, 0.3f);
            result.fontSize = INITIAL_FONT_SIZE * (1 - reduction * 0.5f);
            result.lineHeight = INITIAL_LINE_HEIGHT * (1 - reduction * 0.3f);
            result.sectionMargin = INITIAL_SECTION_MARGIN * (1 - reduction * 0.4f);

            if (result.fontSize < MIN_FONT_SIZE) result.fontSize = MIN_FONT_SIZE;
            if (result.lineHeight < MIN_LINE_HEIGHT) result.lineHeight = MIN_LINE_HEIGHT;
            if (result.sectionMargin < MIN_SECTION_MARGIN) result.sectionMargin = MIN_SECTION_MARGIN;

            if (reduction >= 0.3f && lineCount > 80) {
                result.fitsOnOnePage = false;
                result.warning = "Content too long for one page. Please consider trimming.";
            }
        }
    }
}
