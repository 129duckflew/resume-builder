package com.resume.service;

/**
 * Utility to sanitize CSS content from user input before persisting.
 * Removes known dangerous patterns: {@code @import}, {@code expression()},
 * {@code javascript:} URLs, and IE legacy {@code behavior:} / {@code binding:}.
 */
public class CssSanitizer {

    private CssSanitizer() {}

    /**
     * Sanitize user-supplied CSS by removing dangerous constructs.
     * <p>
     * Strategy: CSS escape sequences (e.g. {@code \6f} for {@code o}) are stripped
     * <em>before</em> pattern matching, preventing bypass of the literal-pattern
     * detectors. This may alter legitimate CSS content values (e.g.
     * {@code content: "\2013"}) but is a deliberate trade-off for security in a
     * user-theme context where such escapes are vanishingly rare.
     *
     * @param css raw CSS input, may be null
     * @return sanitized CSS, never null
     */
    public static String sanitize(String css) {
        if (css == null) return "";
        String result = css;

        // 1. Strip CSS escape sequences so pattern detection cannot be bypassed.
        //    CSS allows \ + 1-6 hex digits + optional whitespace, or \ + any single char.
        result = result.replaceAll("\\\\[0-9a-fA-F]{1,6}\\s?", "");
        result = result.replaceAll("\\\\[^\\s]", "");

        // 2. Remove closing </style> tag to prevent XSS via premature style close.
        result = result.replaceAll("(?i)</style\\s*>", "");

        // 3. Remove @import url(...) and @import "..."
        result = result.replaceAll("(?i)@import\\s+url\\s*\\([^)]*\\)\\s*;?", "");
        result = result.replaceAll("(?i)@import\\s+['\"][^'\"]*['\"]\\s*;?", "");
        // Remove expression(...)
        result = result.replaceAll("(?i)expression\\s*\\(", "");
        // Remove javascript: URLs
        result = result.replaceAll("(?i)javascript\\s*:", "");
        // Remove behavior: and binding: (IE legacy)
        result = result.replaceAll("(?i)behavior\\s*:", "");
        result = result.replaceAll("(?i)binding\\s*:", "");
        return result.trim();
    }
}
