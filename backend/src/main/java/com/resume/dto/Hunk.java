package com.resume.dto;

import java.util.List;

public class Hunk {

    private int oldStart;
    private int oldCount;
    private int newStart;
    private int newCount;
    private List<DiffLine> lines;

    public Hunk() {}

    public Hunk(int oldStart, int oldCount, int newStart, int newCount, List<DiffLine> lines) {
        this.oldStart = oldStart;
        this.oldCount = oldCount;
        this.newStart = newStart;
        this.newCount = newCount;
        this.lines = lines;
    }

    public int getOldStart() { return oldStart; }
    public void setOldStart(int oldStart) { this.oldStart = oldStart; }
    public int getOldCount() { return oldCount; }
    public void setOldCount(int oldCount) { this.oldCount = oldCount; }
    public int getNewStart() { return newStart; }
    public void setNewStart(int newStart) { this.newStart = newStart; }
    public int getNewCount() { return newCount; }
    public void setNewCount(int newCount) { this.newCount = newCount; }
    public List<DiffLine> getLines() { return lines; }
    public void setLines(List<DiffLine> lines) { this.lines = lines; }
}
