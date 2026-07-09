package com.resume.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public class VariableDeclaration {

    private String name;
    private String type;
    private String defaultValue;
    private String label;
    private String group;
    private List<Option> options;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDefaultValue() { return defaultValue; }
    @JsonAlias("default")
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }

    public static class Option {
        private String label;
        private String value;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
