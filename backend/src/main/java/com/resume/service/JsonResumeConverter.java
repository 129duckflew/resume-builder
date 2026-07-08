package com.resume.service;

import com.resume.dto.JsonResumeDTO;
import com.resume.entity.Resume;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JsonResumeConverter {

    public String toMarkdown(JsonResumeDTO dto) {
        StringBuilder md = new StringBuilder();

        if (dto.getBasics() != null) {
            JsonResumeDTO.Basics b = dto.getBasics();
            md.append("# Personal Info\n\n");
            if (b.getName() != null) md.append("Name: ").append(b.getName()).append("\n");
            if (b.getEmail() != null) md.append("Email: ").append(b.getEmail()).append("\n");
            if (b.getPhone() != null) md.append("Phone: ").append(b.getPhone()).append("\n");
            if (b.getUrl() != null) md.append("Website: ").append(b.getUrl()).append("\n");
            if (b.getLocation() != null) {
                JsonResumeDTO.Location loc = b.getLocation();
                String locStr = "";
                if (loc.getCity() != null) locStr += loc.getCity();
                if (loc.getRegion() != null) locStr += (locStr.isEmpty() ? "" : ", ") + loc.getRegion();
                if (!locStr.isEmpty()) md.append("Location: ").append(locStr).append("\n");
            }
            if (b.getProfiles() != null) {
                for (JsonResumeDTO.Profile p : b.getProfiles()) {
                    if (p.getNetwork() != null && p.getUrl() != null) {
                        md.append(p.getNetwork()).append(": ").append(p.getUrl()).append("\n");
                    }
                }
            }
            if (b.getSummary() != null) {
                md.append("\n").append(b.getSummary()).append("\n");
            }
            md.append("\n");
        }

        if (dto.getWork() != null && !dto.getWork().isEmpty()) {
            md.append("# Work Experience\n\n");
            for (JsonResumeDTO.Work w : dto.getWork()) {
                md.append("## ").append(nullToEmpty(w.getName()));
                md.append(" | ").append(nullToEmpty(w.getStartDate())).append(" – ").append(nullToEmpty(w.getEndDate()));
                md.append("\n*").append(nullToEmpty(w.getPosition())).append("*\n");
                if (w.getSummary() != null && !w.getSummary().isEmpty()) {
                    md.append("\n").append(w.getSummary()).append("\n");
                }
                if (w.getHighlights() != null) {
                    for (String h : w.getHighlights()) {
                        md.append("- ").append(h).append("\n");
                    }
                }
                md.append("\n");
            }
        }

        if (dto.getEducation() != null && !dto.getEducation().isEmpty()) {
            md.append("# Education\n\n");
            for (JsonResumeDTO.Education e : dto.getEducation()) {
                md.append("## ").append(nullToEmpty(e.getInstitution()));
                md.append(" | ").append(nullToEmpty(e.getStartDate())).append(" – ").append(nullToEmpty(e.getEndDate()));
                md.append("\n*").append(nullToEmpty(e.getStudyType()));
                if (e.getArea() != null) md.append(" in ").append(e.getArea());
                md.append("*\n");
                if (e.getScore() != null) md.append("- GPA: ").append(e.getScore()).append("\n");
                if (e.getCourses() != null) {
                    for (String c : e.getCourses()) {
                        md.append("- ").append(c).append("\n");
                    }
                }
                md.append("\n");
            }
        }

        if (dto.getSkills() != null && !dto.getSkills().isEmpty()) {
            md.append("# Skills\n\n");
            for (JsonResumeDTO.Skill s : dto.getSkills()) {
                md.append("- ").append(nullToEmpty(s.getName()));
                if (s.getLevel() != null) md.append(" (").append(s.getLevel()).append(")");
                if (s.getKeywords() != null && !s.getKeywords().isEmpty()) {
                    md.append(": ").append(String.join(", ", s.getKeywords()));
                }
                md.append("\n");
            }
            md.append("\n");
        }

        if (dto.getProjects() != null && !dto.getProjects().isEmpty()) {
            md.append("# Projects\n\n");
            for (JsonResumeDTO.Project p : dto.getProjects()) {
                md.append("## ").append(nullToEmpty(p.getName())).append("\n");
                if (p.getDescription() != null) md.append("- Description: ").append(p.getDescription()).append("\n");
                if (p.getKeywords() != null && !p.getKeywords().isEmpty()) {
                    md.append("- Technology: ").append(String.join(", ", p.getKeywords())).append("\n");
                }
                if (p.getUrl() != null) md.append("- URL: ").append(p.getUrl()).append("\n");
                if (p.getStartDate() != null) md.append("- ").append(p.getStartDate());
                if (p.getEndDate() != null) md.append(" – ").append(p.getEndDate());
                if (p.getStartDate() != null) md.append("\n");
                if (p.getHighlights() != null) {
                    for (String h : p.getHighlights()) {
                        md.append("- ").append(h).append("\n");
                    }
                }
                md.append("\n");
            }
        }

        if (dto.getCertificates() != null && !dto.getCertificates().isEmpty()) {
            md.append("# Certificates\n\n");
            for (JsonResumeDTO.Certificate c : dto.getCertificates()) {
                md.append("- ").append(nullToEmpty(c.getName()));
                if (c.getIssuer() != null) md.append(" | ").append(c.getIssuer());
                if (c.getDate() != null) md.append(" | ").append(c.getDate());
                md.append("\n");
            }
            md.append("\n");
        }

        if (dto.getLanguages() != null && !dto.getLanguages().isEmpty()) {
            md.append("# Languages\n\n");
            for (JsonResumeDTO.Language l : dto.getLanguages()) {
                md.append("- ").append(nullToEmpty(l.getLanguage()));
                if (l.getFluency() != null) md.append(" (").append(l.getFluency()).append(")");
                md.append("\n");
            }
            md.append("\n");
        }

        if (dto.getInterests() != null && !dto.getInterests().isEmpty()) {
            md.append("# Interests\n\n");
            for (JsonResumeDTO.Interest i : dto.getInterests()) {
                md.append("- ").append(nullToEmpty(i.getName()));
                if (i.getKeywords() != null && !i.getKeywords().isEmpty()) {
                    md.append(": ").append(String.join(", ", i.getKeywords()));
                }
                md.append("\n");
            }
            md.append("\n");
        }

        if (dto.getVolunteer() != null && !dto.getVolunteer().isEmpty()) {
            md.append("# Volunteer\n\n");
            for (JsonResumeDTO.Volunteer v : dto.getVolunteer()) {
                md.append("## ").append(nullToEmpty(v.getOrganization()));
                if (v.getStartDate() != null || v.getEndDate() != null) {
                    md.append(" | ").append(nullToEmpty(v.getStartDate())).append(" – ").append(nullToEmpty(v.getEndDate()));
                }
                md.append("\n*").append(nullToEmpty(v.getPosition())).append("*\n");
                if (v.getSummary() != null) md.append(v.getSummary()).append("\n");
                if (v.getHighlights() != null) {
                    for (String h : v.getHighlights()) {
                        md.append("- ").append(h).append("\n");
                    }
                }
                md.append("\n");
            }
        }

        if (dto.getAwards() != null && !dto.getAwards().isEmpty()) {
            md.append("# Awards\n\n");
            for (JsonResumeDTO.Award a : dto.getAwards()) {
                md.append("- ").append(nullToEmpty(a.getTitle()));
                if (a.getAwarder() != null) md.append(" | ").append(a.getAwarder());
                if (a.getDate() != null) md.append(" | ").append(a.getDate());
                md.append("\n");
                if (a.getSummary() != null) md.append("  ").append(a.getSummary()).append("\n");
            }
            md.append("\n");
        }

        if (dto.getPublications() != null && !dto.getPublications().isEmpty()) {
            md.append("# Publications\n\n");
            for (JsonResumeDTO.Publication p : dto.getPublications()) {
                md.append("## ").append(nullToEmpty(p.getName())).append("\n");
                if (p.getPublisher() != null) md.append("- Publisher: ").append(p.getPublisher()).append("\n");
                if (p.getReleaseDate() != null) md.append("- Date: ").append(p.getReleaseDate()).append("\n");
                if (p.getSummary() != null) md.append("- ").append(p.getSummary()).append("\n");
                if (p.getUrl() != null) md.append("- URL: ").append(p.getUrl()).append("\n");
                md.append("\n");
            }
        }

        if (dto.getReferences() != null && !dto.getReferences().isEmpty()) {
            md.append("# References\n\n");
            for (JsonResumeDTO.Reference r : dto.getReferences()) {
                md.append("- ").append(nullToEmpty(r.getName()));
                if (r.getReference() != null) md.append(": ").append(r.getReference());
                md.append("\n");
            }
            md.append("\n");
        }

        return md.toString().trim();
    }

    public JsonResumeDTO fromResume(Resume resume) {
        JsonResumeDTO dto = new JsonResumeDTO();
        String content = resume.getContent();
        if (content == null) return dto;

        // Split by top-level headings using MULTILINE
        String[] sections = Pattern.compile("(?m)^(?=# )").split(content);
        for (String section : sections) {
            section = section.trim();
            if (section.isEmpty()) continue;

            String title = extractHeading(section, 1);
            if (title == null) continue;

            String body = section.replaceFirst("^# .+\n?", "").trim();

            switch (title) {
                case "Personal Info":
                case "Contact":
                    dto.setBasics(parseBasics(body));
                    break;
                case "Work Experience":
                case "Experience":
                    dto.setWork(parseWork(body));
                    break;
                case "Education":
                    dto.setEducation(parseEducation(body));
                    break;
                case "Skills":
                    dto.setSkills(parseSkills(body));
                    break;
                case "Projects":
                    dto.setProjects(parseProjects(body));
                    break;
                case "Certificates":
                case "Certifications":
                    dto.setCertificates(parseCertificates(body));
                    break;
                case "Languages":
                    dto.setLanguages(parseLanguages(body));
                    break;
                case "Interests":
                    dto.setInterests(parseInterests(body));
                    break;
                case "Volunteer":
                    dto.setVolunteer(parseVolunteer(body));
                    break;
                case "Awards":
                    dto.setAwards(parseAwards(body));
                    break;
                case "Publications":
                    dto.setPublications(parsePublications(body));
                    break;
                case "References":
                    dto.setReferences(parseReferences(body));
                    break;
            }
        }

        return dto;
    }

    public String markdownFromResume(Resume resume) {
        return resume.getContent();
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private String extractHeading(String text, int level) {
        Pattern p = Pattern.compile("^" + "#".repeat(level) + "\\s+(.+)$", Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    // --- Import helpers ---

    private JsonResumeDTO.Basics parseBasics(String body) {
        JsonResumeDTO.Basics b = new JsonResumeDTO.Basics();
        b.setName(extractLineValue(body, "Name:"));
        b.setEmail(extractLineValue(body, "Email:"));
        b.setPhone(extractLineValue(body, "Phone:"));
        b.setUrl(extractLineValue(body, "Website:"));
        String loc = extractLineValue(body, "Location:");
        if (loc != null) {
            JsonResumeDTO.Location l = new JsonResumeDTO.Location();
            String[] parts = loc.split(",", 2);
            l.setCity(parts[0].trim());
            if (parts.length > 1) l.setRegion(parts[1].trim());
            b.setLocation(l);
        }
        return b;
    }

    private Pattern H2_SPLIT = Pattern.compile("(?m)^(?=## )");

    private List<JsonResumeDTO.Work> parseWork(String body) {
        List<JsonResumeDTO.Work> result = new ArrayList<>();
        String[] entries = H2_SPLIT.split(body);
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            JsonResumeDTO.Work w = new JsonResumeDTO.Work();
            String titleLine = extractHeading(entry, 2);
            if (titleLine != null) {
                String[] parts = titleLine.split("\\s*\\|\\s*");
                w.setName(parts[0].trim());
                if (parts.length > 1) {
                    String[] dates = parts[1].split("\\s*–\\s*");
                    w.setStartDate(dates[0].trim());
                    if (dates.length > 1) w.setEndDate(dates[1].trim());
                }
            }
            String position = extractRegexValue(entry, "^\\*(.+?)\\*");
            if (position != null) w.setPosition(position);
            List<String> highlights = extractBullets(entry);
            if (!highlights.isEmpty()) w.setHighlights(highlights);
            result.add(w);
        }
        return result;
    }

    private List<JsonResumeDTO.Education> parseEducation(String body) {
        List<JsonResumeDTO.Education> result = new ArrayList<>();
        String[] entries = H2_SPLIT.split(body);
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            JsonResumeDTO.Education e = new JsonResumeDTO.Education();
            String titleLine = extractHeading(entry, 2);
            if (titleLine != null) {
                String[] parts = titleLine.split("\\s*\\|\\s*");
                e.setInstitution(parts[0].trim());
                if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                    String[] dates = parts[1].split("\\s*–\\s*");
                    if (dates.length > 0 && !dates[0].trim().isEmpty()) {
                        e.setStartDate(dates[0].trim());
                    }
                    if (dates.length > 1 && !dates[1].trim().isEmpty()) {
                        e.setEndDate(dates[1].trim());
                    }
                }
            }
            String degree = extractRegexValue(entry, "^\\*(.+?)\\*");
            if (degree != null) {
                if (degree.contains(" in ")) {
                    String[] degreeParts = degree.split(" in ", 2);
                    e.setStudyType(degreeParts[0].trim());
                    if (degreeParts.length > 1) e.setArea(degreeParts[1].trim());
                } else {
                    e.setStudyType(degree);
                }
            }
            List<String> bullets = extractBullets(entry);
            List<String> nonGpaBullets = new ArrayList<>();
            for (String bullet : bullets) {
                if (bullet.startsWith("GPA:") || bullet.startsWith("GPA :")) {
                    String gpaVal = bullet.replaceFirst("^GPA\\s*:\\s*", "").trim();
                    if (!gpaVal.isEmpty()) e.setScore(gpaVal);
                } else {
                    nonGpaBullets.add(bullet);
                }
            }
            if (!nonGpaBullets.isEmpty()) e.setCourses(nonGpaBullets);
            result.add(e);
        }
        return result;
    }

    private List<JsonResumeDTO.Skill> parseSkills(String body) {
        List<JsonResumeDTO.Skill> result = new ArrayList<>();
        for (String line : body.split("\n")) {
            line = line.trim();
            if (!line.startsWith("- ")) continue;
            line = line.substring(2);
            JsonResumeDTO.Skill s = new JsonResumeDTO.Skill();
            int parenIdx = line.indexOf('(');
            int colonIdx = line.indexOf(':');
            if (parenIdx > 0 && (colonIdx < 0 || parenIdx < colonIdx)) {
                s.setName(line.substring(0, parenIdx).trim());
                int closeParen = line.indexOf(')', parenIdx);
                if (closeParen > 0) {
                    s.setLevel(line.substring(parenIdx + 1, closeParen).trim());
                }
                if (colonIdx > closeParen) {
                    String keywords = line.substring(colonIdx + 1).trim();
                    s.setKeywords(List.of(keywords.split("\\s*,\\s*")));
                }
            } else if (colonIdx > 0) {
                s.setName(line.substring(0, colonIdx).trim());
                String keywords = line.substring(colonIdx + 1).trim();
                s.setKeywords(List.of(keywords.split("\\s*,\\s*")));
            } else {
                s.setName(line);
            }
            result.add(s);
        }
        return result;
    }

    private List<JsonResumeDTO.Project> parseProjects(String body) {
        List<JsonResumeDTO.Project> result = new ArrayList<>();
        String[] entries = H2_SPLIT.split(body);
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            JsonResumeDTO.Project p = new JsonResumeDTO.Project();
            String titleLine = extractHeading(entry, 2);
            if (titleLine != null) p.setName(titleLine);
            p.setDescription(extractLineValue(entry, "Description:\\s*(.+)"));
            String tech = extractLineValue(entry, "Technology:\\s*(.+)");
            if (tech != null) p.setKeywords(List.of(tech.split("\\s*,\\s*")));
            p.setUrl(extractLineValue(entry, "URL:\\s*(.+)"));
            result.add(p);
        }
        return result;
    }

    private List<JsonResumeDTO.Certificate> parseCertificates(String body) {
        List<JsonResumeDTO.Certificate> result = new ArrayList<>();
        for (String line : body.split("\n")) {
            line = line.trim();
            if (!line.startsWith("- ")) continue;
            line = line.substring(2);
            String[] parts = line.split("\\s*\\|\\s*");
            JsonResumeDTO.Certificate c = new JsonResumeDTO.Certificate();
            c.setName(parts[0].trim());
            if (parts.length > 1) c.setIssuer(parts[1].trim());
            if (parts.length > 2) c.setDate(parts[2].trim());
            result.add(c);
        }
        return result;
    }

    private List<JsonResumeDTO.Language> parseLanguages(String body) {
        List<JsonResumeDTO.Language> result = new ArrayList<>();
        for (String line : body.split("\n")) {
            line = line.trim();
            if (!line.startsWith("- ")) continue;
            line = line.substring(2);
            JsonResumeDTO.Language l = new JsonResumeDTO.Language();
            int parenIdx = line.indexOf('(');
            if (parenIdx > 0) {
                l.setLanguage(line.substring(0, parenIdx).trim());
                int closeParen = line.indexOf(')', parenIdx);
                if (closeParen > 0) l.setFluency(line.substring(parenIdx + 1, closeParen).trim());
            } else {
                l.setLanguage(line.trim());
            }
            result.add(l);
        }
        return result;
    }

    private List<JsonResumeDTO.Interest> parseInterests(String body) {
        List<JsonResumeDTO.Interest> result = new ArrayList<>();
        for (String line : body.split("\n")) {
            line = line.trim();
            if (!line.startsWith("- ")) continue;
            line = line.substring(2);
            JsonResumeDTO.Interest i = new JsonResumeDTO.Interest();
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                i.setName(line.substring(0, colonIdx).trim());
                i.setKeywords(List.of(line.substring(colonIdx + 1).trim().split("\\s*,\\s*")));
            } else {
                i.setName(line.trim());
            }
            result.add(i);
        }
        return result;
    }

    private List<JsonResumeDTO.Volunteer> parseVolunteer(String body) {
        List<JsonResumeDTO.Volunteer> result = new ArrayList<>();
        String[] entries = H2_SPLIT.split(body);
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            JsonResumeDTO.Volunteer v = new JsonResumeDTO.Volunteer();
            String titleLine = extractHeading(entry, 2);
            if (titleLine != null) {
                String[] parts = titleLine.split("\\s*\\|\\s*");
                v.setOrganization(parts[0].trim());
                if (parts.length > 1) {
                    String[] dates = parts[1].split("\\s*–\\s*");
                    v.setStartDate(dates[0].trim());
                    if (dates.length > 1) v.setEndDate(dates[1].trim());
                }
            }
            v.setPosition(extractLineValue(entry, "\\*(.*?)\\*"));
            List<String> highlights = extractBullets(entry);
            if (!highlights.isEmpty()) v.setHighlights(highlights);
            result.add(v);
        }
        return result;
    }

    private List<JsonResumeDTO.Award> parseAwards(String body) {
        List<JsonResumeDTO.Award> result = new ArrayList<>();
        for (String line : body.split("\n")) {
            line = line.trim();
            if (!line.startsWith("- ")) continue;
            line = line.substring(2);
            String[] parts = line.split("\\s*\\|\\s*");
            JsonResumeDTO.Award a = new JsonResumeDTO.Award();
            a.setTitle(parts[0].trim());
            if (parts.length > 1) a.setAwarder(parts[1].trim());
            if (parts.length > 2) a.setDate(parts[2].trim());
            result.add(a);
        }
        return result;
    }

    private List<JsonResumeDTO.Publication> parsePublications(String body) {
        List<JsonResumeDTO.Publication> result = new ArrayList<>();
        String[] entries = H2_SPLIT.split(body);
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            JsonResumeDTO.Publication p = new JsonResumeDTO.Publication();
            String titleLine = extractHeading(entry, 2);
            if (titleLine != null) p.setName(titleLine);
            p.setPublisher(extractLineValue(entry, "Publisher:\\s*(.+)"));
            p.setReleaseDate(extractLineValue(entry, "Date:\\s*(.+)"));
            p.setSummary(extractLineValue(entry, "URL:\\s*(.+)"));
            p.setUrl(extractLineValue(entry, "URL:\\s*(.+)"));
            result.add(p);
        }
        return result;
    }

    private List<JsonResumeDTO.Reference> parseReferences(String body) {
        List<JsonResumeDTO.Reference> result = new ArrayList<>();
        for (String line : body.split("\n")) {
            line = line.trim();
            if (!line.startsWith("- ")) continue;
            line = line.substring(2);
            JsonResumeDTO.Reference r = new JsonResumeDTO.Reference();
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                r.setName(line.substring(0, colonIdx).trim());
                r.setReference(line.substring(colonIdx + 1).trim());
            } else {
                r.setName(line.trim());
            }
            result.add(r);
        }
        return result;
    }

    private String extractLineValue(String text, String prefix) {
        Pattern p = Pattern.compile("(?m)^" + Pattern.quote(prefix) + "\\s*(.+)$");
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    private String extractRegexValue(String text, String regex) {
        Pattern p = Pattern.compile("(?m)" + regex);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    private List<String> extractBullets(String text) {
        List<String> bullets = new ArrayList<>();
        Pattern p = Pattern.compile("(?m)^-\\s+(.+)$");
        Matcher m = p.matcher(text);
        while (m.find()) {
            bullets.add(m.group(1).trim());
        }
        return bullets;
    }
}
