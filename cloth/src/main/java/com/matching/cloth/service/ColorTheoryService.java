package com.matching.cloth.service;

import com.matching.cloth.models.ColorTheory;
import com.matching.cloth.repositories.ColorTheoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColorTheoryService {

    @Autowired
    private ColorTheoryRepository colorTheoryRepository;

    public List<ColorTheory> getAllColorTheories() {
        return colorTheoryRepository.findAll();
    }

    public ColorTheory getColorTheoryById(int id) {
        return colorTheoryRepository.findById(id).orElse(null);
    }
}
