package com.matching.cloth.repositories;

import com.matching.cloth.models.ClothingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothTypeRepository extends JpaRepository<ClothingType, Integer> {
}
