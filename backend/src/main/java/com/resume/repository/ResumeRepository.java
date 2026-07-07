package com.resume.repository;

import com.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, String> {
    List<Resume> findAllByOrderByUpdatedAtDesc();
}
