package com.matching.cloth.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.matching.cloth.models.Astrologer;
import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyListColor;
import com.matching.cloth.repositories.AstrologerRepository;

@Service
public class AstrologerService {

    @Autowired
    private AstrologerRepository astrologerRepository;

    public Astrologer saveOrUpdateAstrologer(Astrologer astrologer) {
        return astrologerRepository.save(astrologer);
    }

    public List<Astrologer> findAllAstrologer() {
        return astrologerRepository.findAll(
                Sort.by(
                        Sort.Order.asc("astrologerName")));
    }

    public Optional<Astrologer> findAstrologerById(Integer id) {
        return astrologerRepository.findById(id);
    }

    public Astrologer findAstrologerByName(String astrologerName) {
        return astrologerRepository.findByAstrologerName(astrologerName);
    }

    public Astrologer findAstrologerById(Long id) {
        Optional<Astrologer> optional = astrologerRepository.findById(id.intValue());
        return optional.orElse(null);
    }

    public boolean isAstrologerExists(String astrologerName) {
        return astrologerRepository.existsByAstrologerName(astrologerName);
    }

    public boolean deleteAstrologerById(Integer id) {
        Optional<Astrologer> optional = astrologerRepository.findById(id);
        if (optional.isEmpty())
            return false;

        Astrologer astrologer = optional.get();

        astrologerRepository.delete(astrologer);
        return true;
    }

}
