package com.resume.repository;

import com.resume.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, String> {

    List<ShareLink> findByResumeIdOrderByCreatedAtDesc(String resumeId);

    Optional<ShareLink> findByIdAndEnabledTrue(String id);
}
