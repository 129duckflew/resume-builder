package com.resume.repository;

import com.resume.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, String> {
    List<Theme> findAllByOrderBySortOrderAsc();

    List<Theme> findByBuiltInTrueOrderBySortOrderAsc();

    List<Theme> findByBuiltInTrueOrUserIdOrderBySortOrderAsc(Long userId);

    Optional<Theme> findByIdAndUserId(String id, Long userId);
}
