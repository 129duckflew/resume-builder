package com.resume.pdf.config;

import com.microsoft.playwright.Browser;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("browser")
public class BrowserHealthIndicator implements HealthIndicator {

    private final Browser browser;

    public BrowserHealthIndicator(Optional<Browser> browser) {
        this.browser = browser.orElse(null);
    }

    @Override
    public Health health() {
        if (browser == null) {
            return Health.down().withDetail("reason", "browser not launched").build();
        }
        if (!browser.isConnected()) {
            return Health.down().withDetail("reason", "browser disconnected").build();
        }
        return Health.up().build();
    }
}
