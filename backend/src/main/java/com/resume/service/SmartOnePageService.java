package com.resume.service;

import com.resume.entity.Resume;
import org.springframework.stereotype.Service;

@Service
public class SmartOnePageService {

    private static final float A4_PAGE_HEIGHT_MM = 297f;
    private static final float A4_PAGE_HEIGHT_PT = 842f;
    private static final float MIN_FONT_SIZE = 8f;
    private static final float MIN_LINE_HEIGHT = 1.2f;
    private static final float MIN_SECTION_MARGIN = 4f;
    private static final float INITIAL_FONT_SIZE = 11f;
    private static final float INITIAL_LINE_HEIGHT = 1.4f;
    private static final float INITIAL_SECTION_MARGIN = 16f;

    public static class AdjustmentResult {
        public float fontSize = INITIAL_FONT_SIZE;
        public float lineHeight = INITIAL_LINE_HEIGHT;
        public float sectionMargin = INITIAL_SECTION_MARGIN;
        public boolean fitsOnOnePage = true;
        public String warning;
    }

    public AdjustmentResult calculateOptimalSettings(Resume resume, String htmlContent) {
        AdjustmentResult result = new AdjustmentResult();

        // Apply initial adjustments from resume if set
        if (resume.getFontSize() != null && resume.getFontSize() > 0) {
            result.fontSize = resume.getFontSize();
        }
        if (resume.getLineHeight() != null && resume.getLineHeight() > 0) {
            result.lineHeight = resume.getLineHeight();
        }

        // In a real implementation, this would use Playwright to:
        // 1. Render HTML in an A4 viewport
        // 2. Measure content height
        // 3. If exceeds page, progressively adjust font-size, line-height, margins
        // 4. Binary search for optimal values
        // 5. If still exceeds after hitting mins, set fitsOnOnePage=false

        // For now, this is a placeholder that estimates based on content length
        estimateFromContent(htmlContent, result);

        return result;
    }

    private void estimateFromContent(String html, AdjustmentResult result) {
        // Simple estimation based on content length
        // Real implementation uses headless browser rendering
        int textLength = html.replaceAll("<[^>]*>", "").length();
        int lineCount = textLength / 80;

        // Rough A4 capacity estimate at default settings: ~60 lines
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
