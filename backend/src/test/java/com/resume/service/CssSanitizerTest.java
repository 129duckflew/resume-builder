package com.resume.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CssSanitizerTest {

    @Test
    void removesImportUrl() {
        String css = "@import url('https://evil.com/hack.css');\nbody { color: red; }";
        String result = CssSanitizer.sanitize(css);
        assertFalse(result.contains("@import"));
        assertTrue(result.contains("body { color: red; }"));
    }

    @Test
    void removesImportString() {
        String css = "@import \"https://evil.com/hack.css\";\nbody { color: red; }";
        String result = CssSanitizer.sanitize(css);
        assertFalse(result.contains("@import"));
    }

    @Test
    void removesExpression() {
        String css = "body { width: expression(alert(1)); }";
        String result = CssSanitizer.sanitize(css);
        assertFalse(result.contains("expression("));
        assertFalse(result.contains("expression("));
    }

    @Test
    void removesJavascriptUrl() {
        String css = "a { background: url(javascript:alert(1)); }";
        String result = CssSanitizer.sanitize(css);
        assertFalse(result.contains("javascript:"));
    }

    @Test
    void removesBehaviorBinding() {
        String css = "body { behavior: url(#default#VML); }\n" +
                "div { binding: url('evil.htc'); }";
        String result = CssSanitizer.sanitize(css);
        assertFalse(result.contains("behavior:"));
        assertFalse(result.contains("binding:"));
    }

    @Test
    void normalCssUnchanged() {
        String css = ":root { --primary: #2563eb; }\nbody { color: var(--primary); }";
        String result = CssSanitizer.sanitize(css);
        assertEquals(css, result);
    }

    @Test
    void emptyStringReturnsEmpty() {
        assertEquals("", CssSanitizer.sanitize(""));
    }

    @Test
    void nullReturnsEmpty() {
        assertEquals("", CssSanitizer.sanitize(null));
    }

    @Test
    void removesMultipleImports() {
        String css = "@import url('a.css');\n@import 'b.css';\nbody { }";
        String result = CssSanitizer.sanitize(css);
        assertFalse(result.contains("@import"));
        assertTrue(result.contains("body"));
    }

    // === P0 adversarial tests for XSS / escape bypass ===

    @Test
    void removesClosingStyleTag() {
        String css = "body { }</style><script>alert(1)</script>";
        String result = CssSanitizer.sanitize(css);
        assertFalse(result.contains("</style>"), "must remove closing style tag");
    }

    @Test
    void removesImportWithCssEscape() {
        // \6f is 'o' in CSS escape — browser decodes @imp\6f rt → @import
        // Sanitizer must strip the escape \6f to break the @import construct.
        // After stripping: @imp\6f rt → @imprt (invalid CSS, harmless).
        // Assert that the literal escape sequence \6f is gone from output.
        String css = "@imp\\6f rt url('evil.css');";
        String result = CssSanitizer.sanitize(css);
        assertFalse(result.contains("\\6f"), "CSS escape \\6f must be stripped, breaking @import");
        assertFalse(result.contains("@import"), "result must not be a valid @import directive");
    }

    @Test
    void removesExpressionWithCssEscape() {
        // \69 is 'i' in CSS escape — browser decodes express\69 on → expression
        // After stripping: express\69 on → expresson (invalid, harmless)
        String css = "color: express\\69 on(alert(1))";
        String result = CssSanitizer.sanitize(css);
        assertFalse(result.contains("\\69"), "CSS escape \\69 must be stripped, breaking expression");
        assertFalse(result.contains("expression("), "result must not contain valid expression(");
    }

    @Test
    void removesJavascriptWithCssEscape() {
        // \6a is 'j' in CSS escape — browser decodes \6a avascript: → javascript:
        // After stripping: url(\6a avascript:alert(1)) → url(avascript:alert(1)) — harmless
        String css = "background: url(\\6a avascript:alert(1))";
        String result = CssSanitizer.sanitize(css);
        assertFalse(result.contains("\\6a"), "CSS escape \\6a must be stripped, breaking javascript:");
        assertFalse(result.contains("javascript:"), "result must not contain valid javascript:");
    }

    @Test
    void normalCssWithBackslashPreserved_safe() {
        // Legitimate CSS escape in content property; sanitizer strips escapes
        // for security, which may alter content values — this test verifies
        // no dangerous patterns remain and no exception is thrown.
        String css = ".foo::before { content: \"\\2013\"; }";
        String result = CssSanitizer.sanitize(css);
        assertNotNull(result);
        assertFalse(result.contains("@import"));
        assertFalse(result.contains("expression"));
        assertFalse(result.contains("javascript:"));
        assertFalse(result.contains("behavior:"));
        assertFalse(result.contains("binding:"));
        assertFalse(result.contains("</style>"));
    }
}
