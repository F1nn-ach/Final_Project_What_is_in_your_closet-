package com.matching.cloth.repositories;

import com.matching.cloth.models.MainColorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MainColorCategoryRepository extends JpaRepository<MainColorCategory, Integer> {
    Optional<MainColorCategory> findByMainColorName(String mainColorName);
}
