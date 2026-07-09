package com.resume.entity;

import jakarta.persistence.Column;
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

    @Test
    void theme_layout_hasDefaultColumnDefinition() throws Exception {
        java.lang.reflect.Field layoutField = Theme.class.getDeclaredField("layout");
        Column column = layoutField.getAnnotation(Column.class);
        assertNotNull(column, "@Column annotation must be present on layout field");
        String def = column.columnDefinition();
        assertTrue(def.contains("default 'single'"),
                "columnDefinition should contain default 'single' for safe migration, but was: " + def);
    }

    @Test
    void theme_layout_defaultValueInJava() {
        Theme theme = new Theme();
        assertEquals("single", theme.getLayout(), "Java field default must be 'single' for new objects");
    }
}
