package com.matching.cloth.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matching.cloth.models.Astrologer;
import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.service.AstrologerService;
import com.matching.cloth.service.LuckyColorService;

/**
 * Controller for Search Lucky Color by Astrologer functionality
 * Handles searching/filtering lucky colors by astrologer and year
 */
@RestController
@RequestMapping("/api/admin/lucky-color")
public class SearchLuckyColorByAstrologerController {

    @Autowired
    private AstrologerService astrologerService;

    @Autowired
    private LuckyColorService luckyColorService;

    /**
     * Search lucky colors by astrologer and year
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchLuckyColorsByAstrologer(
            @RequestParam("astrologerName") String astrologerName,
            @RequestParam("year") Integer year) {

        try {
            Astrologer astrologer = astrologerService.findAstrologerByName(astrologerName);
            if (astrologer == null) {
                return ResponseEntity.ok(List.of());
            }

            List<LuckyColor> luckyColors = luckyColorService.findByAstrologer(astrologer)
                    .stream()
                    .filter(lc -> lc.getYear() == year)
                    .collect(Collectors.toList());

            List<Map<String, Object>> result = luckyColors.stream().map(lc -> {
                Map<String, Object> map = new HashMap<>();
                map.put("astrologerName", lc.getAstrologer().getAstrologerName());
                map.put("day", lc.getLuckyDayOfWeek());
                map.put("typeId", lc.getLuckyColorType().getLuckyColorTypeId());
                map.put("year", lc.getYear());

                List<Integer> colorIds = lc.getLuckyListColors().stream()
                        .map(item -> item.getColorCategory().getColorCategoryId())
                        .collect(Collectors.toList());
                map.put("colors", colorIds);

                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}
