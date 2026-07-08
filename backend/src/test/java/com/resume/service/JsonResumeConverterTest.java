package com.resume.service;

import com.resume.dto.JsonResumeDTO;
import com.resume.entity.Resume;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonResumeConverterTest {

    private final JsonResumeConverter converter = new JsonResumeConverter();

    @Test
    void toMarkdown_withBasics_generatesPersonalInfo() {
        JsonResumeDTO dto = new JsonResumeDTO();
        JsonResumeDTO.Basics b = new JsonResumeDTO.Basics();
        b.setName("Alice");
        b.setEmail("alice@example.com");
        b.setPhone("13800000000");
        dto.setBasics(b);

        String md = converter.toMarkdown(dto);
        assertTrue(md.contains("# Personal Info"));
        assertTrue(md.contains("Name: Alice"));
        assertTrue(md.contains("Email: alice@example.com"));
        assertTrue(md.contains("Phone: 13800000000"));
    }

    @Test
    void toMarkdown_withWork_generatesWorkExperience() {
        JsonResumeDTO dto = new JsonResumeDTO();
        JsonResumeDTO.Work w = new JsonResumeDTO.Work();
        w.setName("Acme Corp");
        w.setPosition("Engineer");
        w.setStartDate("2020");
        w.setEndDate("2023");
        w.setHighlights(List.of("Built feature X", "Led team Y"));
        dto.setWork(List.of(w));

        String md = converter.toMarkdown(dto);
        assertTrue(md.contains("# Work Experience"));
        assertTrue(md.contains("## Acme Corp"));
        assertTrue(md.contains("*Engineer*"));
        assertTrue(md.contains("- Built feature X"));
    }

    @Test
    void toMarkdown_withEducation_generatesEducation() {
        JsonResumeDTO dto = new JsonResumeDTO();
        JsonResumeDTO.Education e = new JsonResumeDTO.Education();
        e.setInstitution("MIT");
        e.setStudyType("Bachelor");
        e.setArea("CS");
        e.setScore("4.0");
        dto.setEducation(List.of(e));

        String md = converter.toMarkdown(dto);
        assertTrue(md.contains("# Education"));
        assertTrue(md.contains("## MIT"));
        assertTrue(md.contains("*Bachelor in CS*"));
        assertTrue(md.contains("GPA: 4.0"));
    }

    @Test
    void toMarkdown_withSkills_generatesSkills() {
        JsonResumeDTO dto = new JsonResumeDTO();
        JsonResumeDTO.Skill s = new JsonResumeDTO.Skill();
        s.setName("Java");
        s.setLevel("Expert");
        s.setKeywords(List.of("Spring", "Hibernate"));
        dto.setSkills(List.of(s));

        String md = converter.toMarkdown(dto);
        assertTrue(md.contains("# Skills"));
        assertTrue(md.contains("- Java (Expert): Spring, Hibernate"));
    }

    @Test
    void toMarkdown_withAllFields_generatesCompleteContent() {
        JsonResumeDTO dto = new JsonResumeDTO();
        JsonResumeDTO.Basics b = new JsonResumeDTO.Basics();
        b.setName("Alice");
        b.setEmail("a@b.com");
        dto.setBasics(b);

        JsonResumeDTO.Work w = new JsonResumeDTO.Work();
        w.setName("Corp");
        w.setPosition("Dev");
        dto.setWork(List.of(w));

        JsonResumeDTO.Education e = new JsonResumeDTO.Education();
        e.setInstitution("MIT");
        e.setStudyType("BS");
        dto.setEducation(List.of(e));

        JsonResumeDTO.Skill s = new JsonResumeDTO.Skill();
        s.setName("Java");
        dto.setSkills(List.of(s));

        JsonResumeDTO.Certificate c = new JsonResumeDTO.Certificate();
        c.setName("AWS");
        c.setIssuer("Amazon");
        dto.setCertificates(List.of(c));

        JsonResumeDTO.Language l = new JsonResumeDTO.Language();
        l.setLanguage("English");
        l.setFluency("Native");
        dto.setLanguages(List.of(l));

        String md = converter.toMarkdown(dto);
        assertTrue(md.contains("Personal Info"));
        assertTrue(md.contains("Work Experience"));
        assertTrue(md.contains("Education"));
        assertTrue(md.contains("Skills"));
        assertTrue(md.contains("Certificates"));
        assertTrue(md.contains("Languages"));
    }

    @Test
    void toMarkdown_withNullDto_returnsEmpty() {
        assertEquals("", converter.toMarkdown(new JsonResumeDTO()));
    }

    @Test
    void fromResume_withPersonalInfo_parsesBasics() {
        Resume resume = new Resume();
        resume.setContent("# Personal Info\n\nName: Bob\nEmail: bob@test.com\nPhone: 13900000000\n");

        JsonResumeDTO dto = converter.fromResume(resume);
        assertNotNull(dto.getBasics());
        assertEquals("Bob", dto.getBasics().getName());
        assertEquals("bob@test.com", dto.getBasics().getEmail());
        assertEquals("13900000000", dto.getBasics().getPhone());
    }

    @Test
    void fromResume_withWorkExperience_parsesWork() {
        Resume resume = new Resume();
        resume.setContent("# Work Experience\n\n## Corp | 2020 – 2023\n*Engineer*\n- Built X\n- Led Y\n");

        JsonResumeDTO dto = converter.fromResume(resume);
        assertNotNull(dto.getWork());
        assertEquals(1, dto.getWork().size());
        assertEquals("Corp", dto.getWork().get(0).getName());
        assertEquals("Engineer", dto.getWork().get(0).getPosition());
        assertEquals("2020", dto.getWork().get(0).getStartDate());
        assertEquals("2023", dto.getWork().get(0).getEndDate());
        assertTrue(dto.getWork().get(0).getHighlights().contains("Built X"));
    }

    @Test
    void fromResume_withEducation_parsesEducation() {
        Resume resume = new Resume();
        resume.setContent("# Education\n\n## MIT\n*Bachelor in CS*\n- GPA: 3.8\n");

        JsonResumeDTO dto = converter.fromResume(resume);
        assertNotNull(dto.getEducation());
        assertEquals(1, dto.getEducation().size());
        assertEquals("MIT", dto.getEducation().get(0).getInstitution());
        assertEquals("Bachelor", dto.getEducation().get(0).getStudyType());
        assertEquals("CS", dto.getEducation().get(0).getArea());
        assertEquals("3.8", dto.getEducation().get(0).getScore());
    }

    @Test
    void fromResume_withSkills_parsesSkills() {
        Resume resume = new Resume();
        resume.setContent("# Skills\n\n- Java (Expert): Spring, Boot\n- Python\n");

        JsonResumeDTO dto = converter.fromResume(resume);
        assertNotNull(dto.getSkills());
        assertEquals(2, dto.getSkills().size());
        assertEquals("Java", dto.getSkills().get(0).getName());
        assertEquals("Expert", dto.getSkills().get(0).getLevel());
        assertTrue(dto.getSkills().get(0).getKeywords().contains("Spring"));
        assertEquals("Python", dto.getSkills().get(1).getName());
    }

    @Test
    void fromResume_withNullContent_returnsEmptyDto() {
        Resume resume = new Resume();
        resume.setContent(null);

        JsonResumeDTO dto = converter.fromResume(resume);
        assertNull(dto.getBasics());
        assertNull(dto.getWork());
    }

    @Test
    void roundTrip_preservesData() {
        JsonResumeDTO original = new JsonResumeDTO();
        JsonResumeDTO.Basics b = new JsonResumeDTO.Basics();
        b.setName("Alice");
        b.setEmail("a@b.com");
        original.setBasics(b);

        JsonResumeDTO.Work w = new JsonResumeDTO.Work();
        w.setName("Acme");
        w.setPosition("Dev");
        w.setStartDate("2020");
        w.setEndDate("2023");
        w.setHighlights(List.of("Item1"));
        original.setWork(List.of(w));

        JsonResumeDTO.Education e = new JsonResumeDTO.Education();
        e.setInstitution("MIT");
        e.setStudyType("BS");
        e.setArea("CS");
        original.setEducation(List.of(e));

        JsonResumeDTO.Skill s = new JsonResumeDTO.Skill();
        s.setName("Java");
        s.setLevel("Expert");
        s.setKeywords(List.of("Spring"));
        original.setSkills(List.of(s));

        String md = converter.toMarkdown(original);
        Resume resume = new Resume();
        resume.setContent(md);
        JsonResumeDTO result = converter.fromResume(resume);

        assertEquals("Alice", result.getBasics().getName());
        assertEquals("a@b.com", result.getBasics().getEmail());
        assertEquals("Acme", result.getWork().get(0).getName());
        assertEquals("Dev", result.getWork().get(0).getPosition());
        assertEquals("MIT", result.getEducation().get(0).getInstitution());
        assertEquals("BS", result.getEducation().get(0).getStudyType());
        assertEquals("CS", result.getEducation().get(0).getArea());
        assertEquals("Java", result.getSkills().get(0).getName());
        assertEquals("Expert", result.getSkills().get(0).getLevel());
    }
}
