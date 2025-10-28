package com.matching.cloth.service;

import com.matching.cloth.models.ColorCategory;
import com.matching.cloth.repositories.ColorCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ColorCategoryService {

    @Autowired
    private ColorCategoryRepository colorCategoryRepository;

    public List<ColorCategory> findAllColorCategories() {
        return colorCategoryRepository.findAll();
    }

    public Optional<ColorCategory> findByColorCategoryName(String colorCategoryName) {
        return colorCategoryRepository.findByColorCategoryName(colorCategoryName);
    }

    public Optional<ColorCategory> findByColorCategoryNameAndHex(String colorCategoryName, String colorCategoryHex) {
        return colorCategoryRepository.findByColorCategoryNameAndColorCategoryHex(colorCategoryName, colorCategoryHex);
    }

    public ColorCategory save(ColorCategory colorCategory) {
        return colorCategoryRepository.save(colorCategory);
    }

    public ColorCategory findOrCreate(String colorCategoryName, String colorCategoryHex) {
        return findByColorCategoryNameAndHex(colorCategoryName, colorCategoryHex)
                .orElseGet(() -> {
                    ColorCategory newColorCategory = new ColorCategory();
                    newColorCategory.setColorCategoryName(colorCategoryName);
                    newColorCategory.setColorCategoryHex(colorCategoryHex);
                    return save(newColorCategory);
                });
    }

    public void deleteColorCategory(int colorCategoryId) {
        colorCategoryRepository.deleteById(colorCategoryId);
    }

    public Optional<ColorCategory> findById(int colorCategoryId) {
        return colorCategoryRepository.findById(colorCategoryId);
    }
}
