package com.resume.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LayoutSplitterTest {

    private final LayoutSplitter splitter = new LayoutSplitter();

    @Test
    void singleLayout_returnsBodyOnly() {
        Map<String, String> result = splitter.split("# Hello\n\nSome content", "single");
        assertEquals(1, result.size());
        assertTrue(result.containsKey("body"));
        assertEquals("# Hello\n\nSome content", result.get("body"));
    }

    @Test
    void headerBarLayout_returnsBodyOnly() {
        Map<String, String> result = splitter.split("# Hello\n\nSome content", "header-bar");
        assertEquals(1, result.size());
        assertTrue(result.containsKey("body"));
    }

    @Test
    void nullLayout_defaultsToSingle() {
        Map<String, String> result = splitter.split("# Hello", null);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("body"));
    }

    @Test
    void sidebarLeft_contactGoesToSidebar_mainGoesToMain() {
        String md = "## Contact\nEmail: test@test.com\n\n## Experience\nWorked at ACME\n\n## Education\nStudied at MIT";
        Map<String, String> result = splitter.split(md, "sidebar-left");

        assertTrue(result.containsKey("sidebar"));
        assertTrue(result.containsKey("main"));
        assertTrue(result.get("sidebar").contains("Contact"));
        assertTrue(result.get("main").contains("Experience"));
        assertTrue(result.get("main").contains("Education"));
        assertFalse(result.get("sidebar").contains("Experience"));
    }

    @Test
    void sidebarRight_producesBothParts() {
        String md = "## Skills\nJava\n\n## Experience\nWorked";
        Map<String, String> result = splitter.split(md, "sidebar-right");

        assertTrue(result.containsKey("sidebar"));
        assertTrue(result.containsKey("main"));
        assertTrue(result.get("sidebar").contains("Skills"));
        assertTrue(result.get("main").contains("Experience"));
    }

    @Test
    void sidebar_keywordCaseInsensitive() {
        String md = "## CONTACT\nEmail\n\n## WORK HISTORY\nJob";
        Map<String, String> result = splitter.split(md, "sidebar-left");

        assertTrue(result.get("sidebar").toLowerCase().contains("contact"));
        assertTrue(result.get("main").toLowerCase().contains("work history"));
    }

    @Test
    void sidebar_noH2Sections_putsAllInMain() {
        String md = "Just some plain text\n\nMore text without headings";
        Map<String, String> result = splitter.split(md, "sidebar-left");

        assertTrue(result.containsKey("body"));
        assertTrue(result.get("body").contains("Just some plain text"));
        assertFalse(result.containsKey("sidebar"));
        assertFalse(result.containsKey("main"));
    }

    @Test
    void sidebar_noKeywordMatch_returnsBodyKey() {
        String md = "## Projects\nBuilt stuff\n## Awards\nWon things";
        Map<String, String> result = splitter.split(md, "sidebar-left");

        assertTrue(result.containsKey("body"));
        assertTrue(result.get("body").contains("Projects"));
        assertTrue(result.get("body").contains("Awards"));
        assertFalse(result.containsKey("sidebar"));
        assertFalse(result.containsKey("main"));
    }

    @Test
    void split_sidebarRight_noSidebarKeywords_returnsBodyKey() {
        String md = "# Name\n## Experience\nfoo\n## Education\nbar";
        Map<String, String> result = splitter.split(md, "sidebar-right");

        assertTrue(result.containsKey("body"));
        assertFalse(result.containsKey("sidebar"));
        assertFalse(result.containsKey("main"));
        assertTrue(result.get("body").contains("Experience"));
        assertTrue(result.get("body").contains("Education"));
    }

    @Test
    void split_sidebarLeft_noSidebarKeywords_returnsBodyKey() {
        String md = "# Name\n## Experience\nfoo\n## Education\nbar";
        Map<String, String> result = splitter.split(md, "sidebar-left");

        assertTrue(result.containsKey("body"));
        assertFalse(result.containsKey("sidebar"));
        assertFalse(result.containsKey("main"));
        assertTrue(result.get("body").contains("Experience"));
        assertTrue(result.get("body").contains("Education"));
    }

    @Test
    void split_sidebarRight_withSidebarKeywords_returnsSidebarAndMain() {
        String md = "# Name\n## Skills\nfoo\n## Experience\nbar";
        Map<String, String> result = splitter.split(md, "sidebar-right");

        assertTrue(result.containsKey("sidebar"));
        assertTrue(result.containsKey("main"));
        assertFalse(result.containsKey("body"));
        assertTrue(result.get("sidebar").contains("Skills"));
        assertTrue(result.get("main").contains("Experience"));
    }

    @Test
    void emptyString_returnsEmptyBody() {
        Map<String, String> result = splitter.split("", "sidebar-left");
        assertTrue(result.containsKey("body"));
        assertEquals("", result.get("body"));
    }

    @Test
    void sidebar_withMixedKeywordsAndNonKeywords() {
        String md = "## Contact\nEmail\n## Skills\nJava\n## Experience\nWorked\n## Education\nSchool";
        Map<String, String> result = splitter.split(md, "sidebar-left");

        assertTrue(result.get("sidebar").contains("Contact"));
        assertTrue(result.get("sidebar").contains("Skills"));
        assertTrue(result.get("main").contains("Experience"));
        assertTrue(result.get("main").contains("Education"));
    }
}
