package com.resume.repository;

import com.resume.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, Long> {

    List<ResumeVersion> findByResumeIdOrderByVersionNumberDesc(String resumeId);

    Optional<ResumeVersion> findByResumeIdAndVersionNumber(String resumeId, Integer versionNumber);

    Optional<ResumeVersion> findTopByResumeIdOrderByVersionNumberDesc(String resumeId);

    void deleteByResumeId(String resumeId);
}
