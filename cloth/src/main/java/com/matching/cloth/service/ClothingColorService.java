package com.matching.cloth.service;

import com.matching.cloth.models.ClothingColor;
import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.ColorCategory;
import com.matching.cloth.repositories.ClothingColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClothingColorService {

    @Autowired
    private ClothingColorRepository clothingColorRepository;

    public List<ClothingColor> findByClothing(Clothing clothing) {
        return clothingColorRepository.findByClothing(clothing);
    }

    public List<ClothingColor> findByColorCategory(ColorCategory colorCategory) {
        return clothingColorRepository.findByColorCategory(colorCategory);
    }

    public ClothingColor save(ClothingColor clothingColor) {
        return clothingColorRepository.save(clothingColor);
    }

    public void delete(ClothingColor clothingColor) {
        clothingColorRepository.delete(clothingColor);
    }

    public void deleteByClothing(Clothing clothing) {
        List<ClothingColor> clothingColors = findByClothing(clothing);
        clothingColorRepository.deleteAll(clothingColors);
    }
}
