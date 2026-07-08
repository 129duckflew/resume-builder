package com.resume.repository;

import com.resume.entity.SectionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionTemplateRepository extends JpaRepository<SectionTemplate, Long> {

    List<SectionTemplate> findByUserIdOrderBySortOrderAsc(Long userId);

    List<SectionTemplate> findByUserIdIsNullOrderBySortOrderAsc();

    void deleteByUserId(Long userId);
}
