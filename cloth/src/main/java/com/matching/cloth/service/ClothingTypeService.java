package com.matching.cloth.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.matching.cloth.models.ClothingType;
import com.matching.cloth.repositories.ClothingTypeRepository;

@Service
public class ClothingTypeService {
    @Autowired
    private ClothingTypeRepository clothingTypeRepository;

    public List<ClothingType> findAllClothingTypes() {
        return clothingTypeRepository.findAll();
    }

    public Optional<ClothingType> findClothingTypeById(int id) {
        return clothingTypeRepository.findById(id);
    }
}
