package com.matching.cloth.service;

import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.MainColorCategory;
import com.matching.cloth.repositories.MainColorCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MainColorCategoryService {

    @Autowired
    private MainColorCategoryRepository mainColorCategoryRepository;

    public List<MainColorCategory> findAllMainColorCategories() {
        return mainColorCategoryRepository.findAll();
    }

    public Optional<MainColorCategory> findByMainColorName(String mainColorName) {
        return mainColorCategoryRepository.findByMainColorName(mainColorName);
    }

    public MainColorCategory save(MainColorCategory mainColorCategory) {
        return mainColorCategoryRepository.save(mainColorCategory);
    }

    public MainColorCategory findOrCreateMainColorCategory(String mainColorName, String mainHex) {
        return findByMainColorName(mainColorName)
                .orElseGet(() -> {
                    MainColorCategory newMainColorCategory = new MainColorCategory();
                    newMainColorCategory.setMainColorName(mainColorName);
                    newMainColorCategory.setMainHex(mainHex);
                    return save(newMainColorCategory);
                });
    }

    public List<MainColorCategory> findUniqueMainCategoriesByClothes(List<Clothing> clothes) {
        return clothes.stream()
                .map(cloth -> cloth.getSubColorCategory() != null ? cloth.getSubColorCategory().getMainColorCategory() : null)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public void deleteMainColorCategory(int mainColorCategoryId) {
        mainColorCategoryRepository.deleteById(mainColorCategoryId);
    }
}
