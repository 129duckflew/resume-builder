package com.resume.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThemeEntityTest {

    @Test
    void theme_hasVariablesSchemaField() {
        Theme theme = new Theme();
        theme.setId("test");
        theme.setVariablesSchema("[{\"name\":\"--primary-color\",\"type\":\"color\",\"default\":\"#000\"}]");
        
        assertNotNull(theme.getVariablesSchema());
        assertTrue(theme.getVariablesSchema().contains("--primary-color"));
    }

    @Test
    void theme_variablesSchema_canBeNull() {
        Theme theme = new Theme();
        theme.setId("test");
        assertNull(theme.getVariablesSchema());
    }
}
