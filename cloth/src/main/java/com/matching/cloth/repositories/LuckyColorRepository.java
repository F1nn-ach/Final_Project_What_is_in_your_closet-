package com.matching.cloth.repositories;

import com.matching.cloth.models.Astrologer;
import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyColorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LuckyColorRepository extends JpaRepository<LuckyColor, Integer> {
    List<LuckyColor> findByLuckyDayOfWeek(String luckyDayOfWeek);
    List<LuckyColor> findByAstrologer(Astrologer astrologer);
    List<LuckyColor> findByAstrologerAndLuckyDayOfWeek(Astrologer astrologer, String luckyDayOfWeek);
    Optional<LuckyColor> findByAstrologerAndLuckyDayOfWeekAndLuckyColorType(Astrologer astrologer, String luckyDayOfWeek, LuckyColorType luckyColorType);
}
