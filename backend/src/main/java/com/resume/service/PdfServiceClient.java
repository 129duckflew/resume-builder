package com.resume.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * HTTP client for the internal pdf-service (HTML → PDF / render-height conversion).
 * Failure semantics: callers treat any exception as "pdf-service unavailable".
 */
@Service
public class PdfServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PdfServiceClient.class);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration HEALTH_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration RENDER_TIMEOUT = Duration.ofSeconds(60);

    private final RestClient healthClient;
    private final RestClient renderClient;

    public PdfServiceClient(RestClient.Builder builder,
                            @Value("${pdf.service.url:http://localhost:8090}") String baseUrl) {
        this.healthClient = builder
                .requestFactory(requestFactory(HEALTH_TIMEOUT))
                .baseUrl(baseUrl)
                .build();
        this.renderClient = builder
                .requestFactory(requestFactory(RENDER_TIMEOUT))
                .baseUrl(baseUrl)
                .build();
    }

    public boolean isAvailable() {
        try {
            return healthClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode()
                    .is2xxSuccessful();
        } catch (Exception e) {
            log.warn("pdf-service health check failed: {}", e.getMessage());
            return false;
        }
    }

    public byte[] generatePdf(String html) {
        return renderClient.post()
                .uri("/pdf")
                .contentType(MediaType.TEXT_HTML)
                .body(html)
                .retrieve()
                .body(byte[].class);
    }

    public double measureHeight(String html) {
        Map<?, ?> response = renderClient.post()
                .uri("/measure")
                .contentType(MediaType.TEXT_HTML)
                .body(html)
                .retrieve()
                .body(Map.class);
        if (response == null || !(response.get("height") instanceof Number height)) {
            throw new IllegalStateException("pdf-service returned invalid measure response");
        }
        return height.doubleValue();
    }

    private static SimpleClientHttpRequestFactory requestFactory(Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
