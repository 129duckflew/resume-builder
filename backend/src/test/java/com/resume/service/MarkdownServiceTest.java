package com.resume.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownServiceTest {

    private MarkdownService service;

    @BeforeEach
    void setUp() {
        service = new MarkdownService();
    }

    @Test
    void toHtml_withNullInput_returnsEmpty() {
        assertEquals("", service.toHtml(null));
    }

    @Test
    void toHtml_withBlankInput_returnsEmpty() {
        assertEquals("", service.toHtml("   "));
    }

    @Test
    void toHtml_withHeading_convertsCorrectly() {
        String html = service.toHtml("# Hello\n## World\n### foo");
        assertTrue(html.contains("<h1>"));
        assertTrue(html.contains("Hello"));
        assertTrue(html.contains("<h2>"));
        assertTrue(html.contains("World"));
        assertTrue(html.contains("<h3>"));
        assertTrue(html.contains("foo"));
    }

    @Test
    void toHtml_withBoldAndItalic_convertsCorrectly() {
        String html = service.toHtml("**bold** and *italic*");
        assertTrue(html.contains("<strong>bold</strong>"));
        assertTrue(html.contains("<em>italic</em>"));
    }

    @Test
    void toHtml_withUnorderedList_convertsCorrectly() {
        String html = service.toHtml("- item1\n- item2");
        assertTrue(html.contains("<ul>"));
        assertTrue(html.contains("<li>item1</li>"));
        assertTrue(html.contains("<li>item2</li>"));
        assertTrue(html.contains("</ul>"));
    }

    @Test
    void toHtml_withOrderedList_convertsCorrectly() {
        String html = service.toHtml("1. first\n2. second");
        assertTrue(html.contains("<ol>"));
        assertTrue(html.contains("<li>first</li>"));
        assertTrue(html.contains("<li>second</li>"));
    }

    @Test
    void toHtml_withLink_convertsCorrectly() {
        String html = service.toHtml("[text](https://example.com)");
        assertTrue(html.contains("<a href=\"https://example.com\">"));
        assertTrue(html.contains("text"));
    }

    @Test
    void toHtml_withParagraph_convertsCorrectly() {
        String html = service.toHtml("line1\n\nline2");
        assertTrue(html.contains("<p>line1</p>"));
        assertTrue(html.contains("<p>line2</p>"));
    }

    @Test
    void toHtml_withBasicInfoSection_buildsAlignedRows() {
        // Trailing double space on line 3 = commonmark hard break (<br />)
        String html = service.toHtml("# 个人简历\n" +
                "## 基本信息\n" +
                " **姓名**: 张三            **性别**: 男  \n" +
                " **电话**: 13800138000    **邮箱**: zhangsan@example.com ");
        assertTrue(html.contains("<h2>基本信息</h2>"));
        assertTrue(html.contains("<div class=\"resume-basic\">"));
        assertTrue(html.contains("<div class=\"resume-basic-row\"><span><strong>姓名</strong>: 张三</span><span><strong>性别</strong>: 男</span></div>"));
        assertTrue(html.contains("<div class=\"resume-basic-row\"><span><strong>电话</strong>: 13800138000</span><span><strong>邮箱</strong>: zhangsan@example.com</span></div>"));
        // The <p> wrapper must be dropped and tags kept balanced: no leftover <p> or stray </div>
        assertFalse(html.contains("<p><div"));
        String section = html.substring(html.indexOf("<h2>基本信息</h2>"));
        assertEquals(1, countOccurrences(section, "<div class=\"resume-basic\">"));
        assertEquals(3, countOccurrences(section, "</div>"));
        assertEquals(0, countOccurrences(section, "<p>"));
    }

    private int countOccurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
