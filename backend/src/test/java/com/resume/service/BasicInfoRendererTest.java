package com.resume.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasicInfoRendererTest {

    private BasicInfoRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new BasicInfoRenderer();
    }

    @Test
    void render_withNullInput_returnsNull() {
        assertNull(renderer.render(null));
    }

    @Test
    void render_withEmptyInput_returnsEmpty() {
        assertEquals("", renderer.render(""));
    }

    @Test
    void render_withMultiEntryLines_buildsFlexRows() {
        String html = """
                <h2>基本信息</h2>
                <p><strong>姓名</strong>: 张三            <strong>性别</strong>: 男<br />
                <strong>电话</strong>: 13800138000    <strong>邮箱</strong>: zhangsan@example.com</p>""";

        String result = renderer.render(html);

        assertTrue(result.contains("<div class=\"resume-basic\">"));
        assertEquals(2, countOccurrences(result, "<div class=\"resume-basic-row\">"));
        assertEquals(4, countOccurrences(result, "<span>"));
        assertTrue(result.contains("<span><strong>姓名</strong>: 张三</span>"));
        assertTrue(result.contains("<span><strong>性别</strong>: 男</span>"));
        assertTrue(result.contains("<span><strong>电话</strong>: 13800138000</span>"));
        assertTrue(result.contains("<span><strong>邮箱</strong>: zhangsan@example.com</span>"));
    }

    @Test
    void render_withEnglishBasicInfoTitle_alsoBuildsRows() {
        String html = "<h2>Basic Info</h2>\n<p>Name: John Doe    Email: j@x.com</p>";

        String result = renderer.render(html);

        assertTrue(result.contains("<div class=\"resume-basic\">"));
        assertTrue(result.contains("<span>Name: John Doe</span>"));
        assertTrue(result.contains("<span>Email: j@x.com</span>"));
    }

    @Test
    void render_withSingleEntryLine_wrapsAsSingleSpanRow() {
        String html = "<h2>基本信息</h2>\n<p>姓名: 张三    <strong>性别</strong>: 男<br />\n简介文本</p>";

        String result = renderer.render(html);

        assertEquals(2, countOccurrences(result, "<div class=\"resume-basic-row\">"));
        assertEquals(3, countOccurrences(result, "<span>"));
        assertTrue(result.contains("<span>简介文本</span>"));
    }

    @Test
    void render_withNoMultiEntryLines_leavesHtmlUnchanged() {
        String html = "<h2>基本信息</h2>\n<p>只有一行文本</p>";

        assertEquals(html, renderer.render(html));
    }

    @Test
    void render_withNonBasicInfoSection_leavesHtmlUnchanged() {
        String html = "<h2>项目经历</h2>\n<p>item one    item two</p>";

        assertEquals(html, renderer.render(html));
    }

    @Test
    void render_withSingleSpaceSeparator_doesNotSplit() {
        String html = "<h2>基本信息</h2>\n<p>姓名: 张三 性别: 男</p>";

        assertEquals(html, renderer.render(html));
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
