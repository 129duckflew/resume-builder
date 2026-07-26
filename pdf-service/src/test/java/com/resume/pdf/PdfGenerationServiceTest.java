package com.resume.pdf;

import com.microsoft.playwright.Browser;
import com.resume.pdf.service.PdfGenerationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PdfGenerationServiceTest {

    @Test
    void serviceWithoutBrowser_isUnavailable() {
        PdfGenerationService service = new PdfGenerationService(Optional.empty());
        assertFalse(service.isAvailable());
        assertThrows(IllegalStateException.class, () -> service.generatePdf("<html></html>"));
        assertThrows(IllegalStateException.class, () -> service.measureHeight("<html></html>"));
    }

    @Test
    void serviceWithConnectedBrowser_isAvailable() {
        Browser browser = Mockito.mock(Browser.class);
        when(browser.isConnected()).thenReturn(true);
        PdfGenerationService service = new PdfGenerationService(Optional.of(browser));
        assertTrue(service.isAvailable());
    }

    @Test
    void serviceWithDisconnectedBrowser_isUnavailable() {
        Browser browser = Mockito.mock(Browser.class);
        when(browser.isConnected()).thenReturn(false);
        PdfGenerationService service = new PdfGenerationService(Optional.of(browser));
        assertFalse(service.isAvailable());
    }
}
