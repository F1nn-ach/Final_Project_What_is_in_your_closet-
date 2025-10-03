package com.matching.cloth.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.matching.cloth.models.Astrologer;
import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyColorType;

@Repository
public interface LuckyColorRepository extends JpaRepository<LuckyColor, Integer> {
    List<LuckyColor> findByLuckyColorType_LuckyColorTypeName(String luckyColorTypeName);

    List<LuckyColor> findByAstrologer(Astrologer astrologer);

    List<LuckyColor> findAll();

    LuckyColor findByLuckyColorId(int luckyColorId);

    List<LuckyColor> findByLuckyDayOfWeek(String luckyDayOfWeek);

    Optional<LuckyColor> findByAstrologerAndLuckyDayOfWeekAndLuckyColorType(
            Astrologer astrologer,
            String luckyDayOfWeek,
            LuckyColorType luckyColorType);

    List<LuckyColor> findByAstrologerAndLuckyDayOfWeek(Astrologer astrologer, String luckyDayOfWeek);
}
