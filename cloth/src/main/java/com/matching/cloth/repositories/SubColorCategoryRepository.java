package com.matching.cloth.repositories;

import com.matching.cloth.models.MainColorCategory;
import com.matching.cloth.models.SubColorCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubColorCategoryRepository extends JpaRepository<SubColorCategory, Integer> {
    Optional<SubColorCategory> findBySubColorNameAndSubHexAndMainColorCategory(String subColorName, String subHex, MainColorCategory mainColorCategory);
}
