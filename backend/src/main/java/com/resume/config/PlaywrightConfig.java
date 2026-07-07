package com.resume.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Playwright.class)
public class PlaywrightConfig {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightConfig.class);

    @Value("${app.export.chrome-path:/usr/bin/chromium}")
    private String chromePath;

    @Bean
    public Browser playwrightBrowser() {
        try {
            log.info("Initializing Playwright and launching Chromium at: {}", chromePath);
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setExecutablePath(java.nio.file.Path.of(chromePath))
                    .setHeadless(true));
            log.info("Playwright browser launched successfully");
            return browser;
        } catch (Exception e) {
            log.warn("Playwright browser not available ({}). PDF export will use fallback.", e.getMessage());
            return null;
        }
    }
}
