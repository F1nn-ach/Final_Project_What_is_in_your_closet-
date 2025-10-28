package com.matching.cloth.repositories;

import com.matching.cloth.models.ColorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColorCategoryRepository extends JpaRepository<ColorCategory, Integer> {
    Optional<ColorCategory> findByColorCategoryNameAndColorCategoryHex(String colorCategoryName, String colorCategoryHex);
    Optional<ColorCategory> findByColorCategoryName(String colorCategoryName);
}
