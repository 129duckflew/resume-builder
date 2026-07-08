package com.resume.repository;

import com.resume.entity.DesensitizeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesensitizeRuleRepository extends JpaRepository<DesensitizeRule, Long> {

    List<DesensitizeRule> findByUserIdOrderBySortOrderAsc(Long userId);

    List<DesensitizeRule> findByUserIdIsNullOrderBySortOrderAsc();

    void deleteByUserId(Long userId);
}
