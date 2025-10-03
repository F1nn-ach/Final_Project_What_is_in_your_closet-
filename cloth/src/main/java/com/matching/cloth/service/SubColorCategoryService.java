package com.matching.cloth.service;

import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.MainColorCategory;
import com.matching.cloth.models.SubColorCategory;
import com.matching.cloth.repositories.SubColorCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubColorCategoryService {

    @Autowired
    private SubColorCategoryRepository subColorCategoryRepository;

    public List<SubColorCategory> findAllSubColorCategories() {
        return subColorCategoryRepository.findAll();
    }

    public Optional<SubColorCategory> findSubColorCategoryById(int id) {
        return subColorCategoryRepository.findById(id);
    }

    public Optional<SubColorCategory> findSubColorCategoryByNameAndHexAndMainColorCategory(String subColorName,
            String subHex, MainColorCategory mainColorCategory) {
        return subColorCategoryRepository.findBySubColorNameAndSubHexAndMainColorCategory(subColorName, subHex,
                mainColorCategory);
    }

    public SubColorCategory save(SubColorCategory subColorCategory) {
        return subColorCategoryRepository.save(subColorCategory);
    }

    public SubColorCategory findOrCreateSubColorCategory(String subColorName, String subHex,
            MainColorCategory mainColorCategory) {
        return findSubColorCategoryByNameAndHexAndMainColorCategory(subColorName, subHex, mainColorCategory)
                .orElseGet(() -> {
                    SubColorCategory newSubColorCategory = new SubColorCategory();
                    newSubColorCategory.setSubColorName(subColorName);
                    newSubColorCategory.setSubHex(subHex);
                    newSubColorCategory.setMainColorCategory(mainColorCategory);
                    return save(newSubColorCategory);
                });
    }

    public void deleteSubColorCategory(int subColorCategoryId) {
        subColorCategoryRepository.deleteById(subColorCategoryId);
    }

    public List<SubColorCategory> findUniqueSubCategoriesByClothes(List<Clothing> clothes) {
        return clothes.stream()
                .map(Clothing::getSubColorCategory)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

}
