package com.matching.cloth.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.matching.cloth.models.Astrologer;
import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyColorType;
import com.matching.cloth.repositories.LuckyColorRepository;

@Service
public class LuckyColorService {
    @Autowired
    private LuckyColorRepository luckyColorRepository;

    public LuckyColor saveOrUpdateLuckyColor(LuckyColor luckyColor) {
        return luckyColorRepository.save(luckyColor);
    }

    public Optional<LuckyColor> findLuckyColorById(Integer id) {
        return luckyColorRepository.findById(id);
    }

    public Optional<LuckyColor> findDuplicate(Astrologer astrologer, String dayOfWeek, LuckyColorType luckyColorType) {
        return luckyColorRepository.findByAstrologerAndLuckyDayOfWeekAndLuckyColorType(astrologer, dayOfWeek,
                luckyColorType);
    }

    public List<LuckyColor> findByDayOfWeek(String dayOfWeek) {
        return luckyColorRepository.findByLuckyDayOfWeek(dayOfWeek);
    }

    public List<LuckyColor> findByAstrologer(Astrologer astrolger) {
        return luckyColorRepository.findByAstrologer(astrolger);
    }

    public List<LuckyColor> findByAstrologerAndDay(Astrologer astrologer, String dayOfWeek) {
        return luckyColorRepository.findByAstrologerAndLuckyDayOfWeek(astrologer, dayOfWeek);
    }

    public List<LuckyColor> findAllLuckyColors() {
        List<LuckyColor> luckyColors = luckyColorRepository.findAll(
                Sort.by(
                        Sort.Order.asc("astrologer.astrologerName"),
                        Sort.Order.asc("luckyColorType.luckyColorTypeName")));

        List<String> dayOrder = Arrays.asList(
                "อาทิตย์", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์");

        luckyColors.sort(Comparator
                .comparing((LuckyColor lc) -> lc.getAstrologer().getAstrologerName())
                .thenComparing(lc -> dayOrder.indexOf(lc.getLuckyDayOfWeek()))
                .thenComparing(lc -> lc.getLuckyColorType().getLuckyColorTypeName()));

        return luckyColors;
    }

    public boolean deleteLuckyColorById(Integer id) {
        if (luckyColorRepository.findById(id).isPresent()) {
            luckyColorRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean deleteLuckyColorsByIds(List<Integer> ids) {
        if (!luckyColorRepository.findAllById(ids).isEmpty()) {
            luckyColorRepository.deleteAllById(ids);
            return true;
        }
        return false;
    }

}
