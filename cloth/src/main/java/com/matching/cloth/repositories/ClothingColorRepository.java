package com.matching.cloth.repositories;

import com.matching.cloth.models.ClothingColor;
import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.ColorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClothingColorRepository extends JpaRepository<ClothingColor, Integer> {
    List<ClothingColor> findByClothing(Clothing clothing);
    List<ClothingColor> findByColorCategory(ColorCategory colorCategory);
}
