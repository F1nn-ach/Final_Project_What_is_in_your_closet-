package com.matching.cloth.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.matching.cloth.models.ClothingType;
import com.matching.cloth.repositories.ClothTypeRepository;

@Service
public class ClothTypeService {
    @Autowired
    private ClothTypeRepository clothTypeRepository;

    public List<ClothingType> findAllClothTypes() {
        return clothTypeRepository.findAll();
    }

    public Optional<ClothingType> findClothTypeById(int id) {
        return clothTypeRepository.findById(id);
    }
}
