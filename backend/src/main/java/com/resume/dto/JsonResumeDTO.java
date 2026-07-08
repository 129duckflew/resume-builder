package com.resume.dto;

import java.util.List;

public class JsonResumeDTO {

    private Basics basics;
    private List<Work> work;
    private List<Education> education;
    private List<Skill> skills;
    private List<Project> projects;
    private List<Certificate> certificates;
    private List<Language> languages;
    private List<Reference> references;
    private List<Interest> interests;
    private List<Volunteer> volunteer;
    private List<Award> awards;
    private List<Publication> publications;

    public Basics getBasics() { return basics; }
    public void setBasics(Basics basics) { this.basics = basics; }
    public List<Work> getWork() { return work; }
    public void setWork(List<Work> work) { this.work = work; }
    public List<Education> getEducation() { return education; }
    public void setEducation(List<Education> education) { this.education = education; }
    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills; }
    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects; }
    public List<Certificate> getCertificates() { return certificates; }
    public void setCertificates(List<Certificate> certificates) { this.certificates = certificates; }
    public List<Language> getLanguages() { return languages; }
    public void setLanguages(List<Language> languages) { this.languages = languages; }
    public List<Reference> getReferences() { return references; }
    public void setReferences(List<Reference> references) { this.references = references; }
    public List<Interest> getInterests() { return interests; }
    public void setInterests(List<Interest> interests) { this.interests = interests; }
    public List<Volunteer> getVolunteer() { return volunteer; }
    public void setVolunteer(List<Volunteer> volunteer) { this.volunteer = volunteer; }
    public List<Award> getAwards() { return awards; }
    public void setAwards(List<Award> awards) { this.awards = awards; }
    public List<Publication> getPublications() { return publications; }
    public void setPublications(List<Publication> publications) { this.publications = publications; }

    public static class Basics {
        private String name;
        private String email;
        private String phone;
        private String url;
        private String summary;
        private Location location;
        private List<Profile> profiles;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public Location getLocation() { return location; }
        public void setLocation(Location location) { this.location = location; }
        public List<Profile> getProfiles() { return profiles; }
        public void setProfiles(List<Profile> profiles) { this.profiles = profiles; }
    }

    public static class Location {
        private String address;
        private String city;
        private String region;
        private String countryCode;

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    }

    public static class Profile {
        private String network;
        private String username;
        private String url;

        public String getNetwork() { return network; }
        public void setNetwork(String network) { this.network = network; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class Work {
        private String name;
        private String position;
        private String url;
        private String startDate;
        private String endDate;
        private String summary;
        private List<String> highlights;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public List<String> getHighlights() { return highlights; }
        public void setHighlights(List<String> highlights) { this.highlights = highlights; }
    }

    public static class Education {
        private String institution;
        private String url;
        private String area;
        private String studyType;
        private String startDate;
        private String endDate;
        private String score;
        private List<String> courses;

        public String getInstitution() { return institution; }
        public void setInstitution(String institution) { this.institution = institution; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }
        public String getStudyType() { return studyType; }
        public void setStudyType(String studyType) { this.studyType = studyType; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getScore() { return score; }
        public void setScore(String score) { this.score = score; }
        public List<String> getCourses() { return courses; }
        public void setCourses(List<String> courses) { this.courses = courses; }
    }

    public static class Skill {
        private String name;
        private String level;
        private List<String> keywords;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    }

    public static class Project {
        private String name;
        private String description;
        private List<String> highlights;
        private List<String> keywords;
        private String startDate;
        private String endDate;
        private String url;
        private List<String> roles;
        private String entity;
        private String type;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<String> getHighlights() { return highlights; }
        public void setHighlights(List<String> highlights) { this.highlights = highlights; }
        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
        public String getEntity() { return entity; }
        public void setEntity(String entity) { this.entity = entity; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class Certificate {
        private String name;
        private String date;
        private String issuer;
        private String url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class Language {
        private String language;
        private String fluency;

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getFluency() { return fluency; }
        public void setFluency(String fluency) { this.fluency = fluency; }
    }

    public static class Reference {
        private String name;
        private String reference;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }

    public static class Interest {
        private String name;
        private List<String> keywords;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    }

    public static class Volunteer {
        private String organization;
        private String position;
        private String url;
        private String startDate;
        private String endDate;
        private String summary;
        private List<String> highlights;

        public String getOrganization() { return organization; }
        public void setOrganization(String organization) { this.organization = organization; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public List<String> getHighlights() { return highlights; }
        public void setHighlights(List<String> highlights) { this.highlights = highlights; }
    }

    public static class Award {
        private String title;
        private String date;
        private String awarder;
        private String summary;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getAwarder() { return awarder; }
        public void setAwarder(String awarder) { this.awarder = awarder; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    public static class Publication {
        private String name;
        private String publisher;
        private String releaseDate;
        private String url;
        private String summary;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        public String getReleaseDate() { return releaseDate; }
        public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }
}
