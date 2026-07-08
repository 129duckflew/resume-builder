package com.resume.repository;

import com.resume.entity.ResumeStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeStyleRepository extends JpaRepository<ResumeStyle, Long> {

    Optional<ResumeStyle> findByResumeIdAndThemeId(String resumeId, String themeId);
}
