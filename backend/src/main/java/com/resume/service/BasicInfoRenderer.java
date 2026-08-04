package com.resume.service;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Post-processes rendered HTML so that "basic info" sections (e.g. {@code ## 基本信息})
 * align their entries to both edges of the line.
 *
 * Lines holding two or more entries separated by multiple spaces
 * (e.g. {@code **姓名**: 张三            **性别**: 男}) are rebuilt as flex rows with
 * {@code justify-content: space-between}, so the first entry sits at the left edge and
 * the last entry at the right edge — and every row shares the same edges, keeping the
 * entries vertically aligned across lines.
 */
public class BasicInfoRenderer {

    private static final Set<String> BASIC_INFO_TITLES = Set.of(
            "基本信息", "基本资料", "个人信息", "个人资料", "联系方式", "联系信息",
            "basic info", "basic information", "personal info", "personal information",
            "contact", "contact info", "contact information"
    );

    private static final Pattern SECTION_PATTERN = Pattern.compile(
            "(<h2>)(?<title>.*?)(</h2>\\s*<p>)(?<body>.*?)(</p>)",
            Pattern.DOTALL);

    private static final Pattern LINE_BREAK_PATTERN = Pattern.compile("<br\\s*/?>|\\n");

    /** Two or more whitespace characters — the separator between entries on one line. */
    private static final Pattern ENTRY_SEPARATOR_PATTERN = Pattern.compile("\\s{2,}");

    /**
     * @param html already-rendered HTML (from the markdown renderer)
     * @return the same HTML, with basic-info paragraphs rebuilt as aligned rows;
     *         unchanged if no basic-info section with multi-entry lines is present
     */
    public String render(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        Matcher matcher = SECTION_PATTERN.matcher(html);
        StringBuffer result = new StringBuffer();
        boolean changed = false;
        while (matcher.find()) {
            String title = matcher.group("title")
                    .replaceAll("<[^>]*>", "")
                    .trim()
                    .toLowerCase(Locale.ROOT);
            if (!BASIC_INFO_TITLES.contains(title)) {
                continue;
            }
            String rebuilt = rebuildParagraph(matcher.group("body"));
            if (rebuilt == null) {
                continue;
            }
            // Drop the <p> wrapper entirely: the rebuilt block is a <div>, and
            // a <p> cannot contain block elements.
            matcher.appendReplacement(result, Matcher.quoteReplacement(
                    matcher.group(1) + matcher.group("title") + "</h2>\n" + rebuilt));
            changed = true;
        }
        if (!changed) {
            return html;
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Rebuild the inner content of a basic-info paragraph.
     *
     * @return rebuilt content, or {@code null} when no line holds multiple entries
     */
    private String rebuildParagraph(String body) {
        String[] lines = LINE_BREAK_PATTERN.split(body);
        StringBuilder rows = new StringBuilder();
        boolean hasMultiEntryLine = false;
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = ENTRY_SEPARATOR_PATTERN.split(line.trim());
            if (parts.length >= 2) {
                hasMultiEntryLine = true;
                rows.append("\n<div class=\"resume-basic-row\">");
                for (String part : parts) {
                    rows.append("<span>").append(part).append("</span>");
                }
                rows.append("</div>");
            } else {
                rows.append("\n<div class=\"resume-basic-row\"><span>")
                        .append(line.trim())
                        .append("</span></div>");
            }
        }
        if (!hasMultiEntryLine) {
            return null;
        }
        return "<div class=\"resume-basic\">" + rows + "\n</div>";
    }
}
