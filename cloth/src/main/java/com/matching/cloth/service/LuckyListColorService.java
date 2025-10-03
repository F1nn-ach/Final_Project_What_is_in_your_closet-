package com.matching.cloth.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyListColor;
import com.matching.cloth.repositories.LuckyListColorRepository;

@Service
public class LuckyListColorService {
    @Autowired
    private LuckyListColorRepository luckyListColorRepository;

    @Autowired
    private LuckyColorService luckyColorService;

    public List<LuckyListColor> findAllLuckyListColor() {
        return luckyListColorRepository.findAll();
    }

    public Optional<LuckyListColor> findByLuckyListColorId(Integer id) {
        return luckyListColorRepository.findById(id);
    }

    // public boolean deleteLuckyListColor(Integer id) {
    //     Optional<LuckyListColor> optional = luckyListColorRepository.findById(id);
    //     if (optional.isEmpty()) {
    //         return false;
    //     }

    //     LuckyListColor color = optional.get();

    //     luckyListColorRepository.delete(color);
    //     return true;
    // }
}
