package com.matching.cloth.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.matching.cloth.models.LuckyColorType;
import com.matching.cloth.repositories.LuckyColorTypeRepository;

@Service
public class LuckyColorTypeService {
    @Autowired
    private LuckyColorTypeRepository luckyColorTypeRepository;

    public Optional<LuckyColorType> findByLuckyColorTypeId(Integer id) {
        return luckyColorTypeRepository.findById(id);
    }

    public List<LuckyColorType> findAllLuckyColorTypes() {
        return luckyColorTypeRepository.findAllByOrderByLuckyColorTypeNameAsc();

    }
}
