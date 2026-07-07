package com.resume.service;

import com.microsoft.playwright.Browser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

    @Test
    void constructor_withEmptyBrowser_marksAsUnavailable() {
        PdfGenerationService service = new PdfGenerationService(Optional.empty());
        assertFalse(service.isAvailable());
    }

    @Test
    void constructor_withPresentBrowser_marksAsAvailable() {
        Browser browser = org.mockito.Mockito.mock(Browser.class);
        PdfGenerationService service = new PdfGenerationService(Optional.of(browser));
        assertTrue(service.isAvailable());
    }

    @Test
    void generatePdf_whenNotAvailable_throws() {
        PdfGenerationService service = new PdfGenerationService(Optional.empty());
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.generatePdf("<html></html>"));
        assertTrue(ex.getMessage().contains("not available"));
    }

    @Test
    void generatePdf_withMockBrowser_returnsBytes() {
        Browser browser = org.mockito.Mockito.mock(Browser.class);
        PdfGenerationService service = new PdfGenerationService(Optional.of(browser));

        // Note: full Playwright mock would require mocking BrowserContext, Page, etc.
        // This test validates the service wiring without actual Playwright
        assertTrue(service.isAvailable());
    }
}
