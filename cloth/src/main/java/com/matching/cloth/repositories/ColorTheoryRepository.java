package com.matching.cloth.repositories;

import com.matching.cloth.models.ColorTheory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorTheoryRepository extends JpaRepository<ColorTheory, Integer> {
}
