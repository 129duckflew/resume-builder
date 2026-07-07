package com.resume.dto;

public class ThemeDTO {

    private String id;
    private String name;
    private String description;
    private boolean builtIn;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isBuiltIn() { return builtIn; }
    public void setBuiltIn(boolean builtIn) { this.builtIn = builtIn; }
}
