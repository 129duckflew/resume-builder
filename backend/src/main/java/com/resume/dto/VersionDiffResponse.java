package com.resume.dto;

import java.util.List;

public class VersionDiffResponse {

    private VersionMeta versionA;
    private VersionMeta versionB;
    private List<Hunk> hunks;

    public VersionDiffResponse() {}

    public VersionDiffResponse(VersionMeta versionA, VersionMeta versionB, List<Hunk> hunks) {
        this.versionA = versionA;
        this.versionB = versionB;
        this.hunks = hunks;
    }

    public VersionMeta getVersionA() { return versionA; }
    public void setVersionA(VersionMeta versionA) { this.versionA = versionA; }
    public VersionMeta getVersionB() { return versionB; }
    public void setVersionB(VersionMeta versionB) { this.versionB = versionB; }
    public List<Hunk> getHunks() { return hunks; }
    public void setHunks(List<Hunk> hunks) { this.hunks = hunks; }
}
