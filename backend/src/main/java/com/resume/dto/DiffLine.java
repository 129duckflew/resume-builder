package com.resume.dto;

public class DiffLine {

    private LineType type;
    private String text;

    public DiffLine() {}

    public DiffLine(LineType type, String text) {
        this.type = type;
        this.text = text;
    }

    public LineType getType() { return type; }
    public void setType(LineType type) { this.type = type; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
