package com.resume.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

    @Mock
    private PdfServiceClient pdfServiceClient;

    @Test
    void isAvailable_delegatesToClient() {
        when(pdfServiceClient.isAvailable()).thenReturn(false);
        PdfGenerationService service = new PdfGenerationService(pdfServiceClient);
        assertFalse(service.isAvailable());

        when(pdfServiceClient.isAvailable()).thenReturn(true);
        assertTrue(service.isAvailable());
    }

    @Test
    void generatePdf_delegatesToClient() {
        byte[] pdf = "PDF DATA".getBytes();
        when(pdfServiceClient.generatePdf("<html></html>")).thenReturn(pdf);
        PdfGenerationService service = new PdfGenerationService(pdfServiceClient);
        assertArrayEquals(pdf, service.generatePdf("<html></html>"));
    }

    @Test
    void generatePdf_whenClientFails_throwsRuntimeException() {
        when(pdfServiceClient.generatePdf("<html></html>"))
                .thenThrow(new IllegalStateException("connection refused"));
        PdfGenerationService service = new PdfGenerationService(pdfServiceClient);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.generatePdf("<html></html>"));
        assertTrue(ex.getMessage().contains("Failed to generate PDF"));
    }
}
