package com.resume.pdf.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaywrightConfig {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightConfig.class);

    @Bean(destroyMethod = "close")
    public Playwright playwright() {
        log.info("Initializing Playwright");
        return Playwright.create();
    }

    @Bean
    public Browser playwrightBrowser(Playwright playwright) {
        log.info("Launching Chromium");
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true));
        log.info("Chromium launched successfully");
        return browser;
    }
}
