package com.matching.cloth.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.matching.cloth.models.Astrologer;
import com.matching.cloth.models.ColorCategory;
import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyColorType;
import com.matching.cloth.models.LuckyListColor;
import com.matching.cloth.repositories.LuckyColorRepository;

@Service
public class LuckyColorService {
    @Autowired
    private LuckyColorRepository luckyColorRepository;

    @Autowired
    private AstrologerService astrologerService;

    @Autowired
    private LuckyColorTypeService luckyColorTypeService;

    @Autowired
    private ColorCategoryService colorCategoryService;

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

    public void saveLuckyColorsData(String astrologerName, Integer year, String luckyColorsDataJson,
            String selectedTypesJson) {
        Astrologer astrologer = astrologerService.findAstrologerByName(astrologerName);
        if (astrologer == null) {
            if (astrologerName.startsWith("__new__:")) {
                String newName = astrologerName.substring(8);
                astrologer = new Astrologer();
                astrologer.setAstrologerName(newName);
                astrologer = astrologerService.saveOrUpdateAstrologer(astrologer);
            } else {
                throw new IllegalArgumentException("ไม่พบแม่หมอ");
            }
        }

        processAndSaveLuckyColors(astrologer, year, luckyColorsDataJson, selectedTypesJson);
    }

    public void editLuckyColorsData(String astrologerName, Integer year, String luckyColorsDataJson,
            String selectedTypesJson) {
        Astrologer astrologer = astrologerService.findAstrologerByName(astrologerName);
        if (astrologer == null) {
            throw new IllegalArgumentException("ไม่พบแม่หมอที่ต้องการแก้ไข");
        }

        deleteExistingLuckyColors(astrologer, year, selectedTypesJson);
        processAndSaveLuckyColors(astrologer, year, luckyColorsDataJson, selectedTypesJson);
    }

    private void deleteExistingLuckyColors(Astrologer astrologer, Integer year, String selectedTypesJson) {
        List<Integer> selectedTypeIds = parseSelectedTypes(selectedTypesJson);
        String[] daysArray = { "อาทิตย์", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์" };

        for (String day : daysArray) {
            for (Integer typeId : selectedTypeIds) {
                Optional<LuckyColorType> typeOptional = luckyColorTypeService.findByLuckyColorTypeId(typeId);
                if (typeOptional.isPresent()) {
                    Optional<LuckyColor> existing = findDuplicate(astrologer, day, typeOptional.get());
                    if (existing.isPresent() && existing.get().getYear() == year) {
                        deleteLuckyColorsByIds(Arrays.asList(existing.get().getLuckyColorId()));
                    }
                }
            }
        }
    }

    private List<Integer> parseSelectedTypes(String selectedTypesJson) {
        List<Integer> selectedTypeIds = new ArrayList<>();
        if (selectedTypesJson != null && !selectedTypesJson.isEmpty()) {
            String[] typeIds = selectedTypesJson.split(",");
            for (String typeIdStr : typeIds) {
                selectedTypeIds.add(Integer.parseInt(typeIdStr.trim()));
            }
        }
        return selectedTypeIds;
    }

    private void processAndSaveLuckyColors(Astrologer astrologer, Integer year, String luckyColorsDataJson,
            String selectedTypesJson) {
        String[] entries = luckyColorsDataJson.split(";");

        for (String entry : entries) {
            if (entry.trim().isEmpty())
                continue;

            String[] parts = entry.split(":");
            if (parts.length != 2)
                continue;

            String key = parts[0].trim();
            String colorsStr = parts[1].trim();

            String[] keyParts = key.split("-");
            if (keyParts.length != 2)
                continue;

            String day = keyParts[0];
            Integer typeId = Integer.parseInt(keyParts[1]);

            Optional<LuckyColorType> luckyColorTypeOptional = luckyColorTypeService.findByLuckyColorTypeId(typeId);
            if (!luckyColorTypeOptional.isPresent())
                continue;

            LuckyColorType luckyColorType = luckyColorTypeOptional.get();

            LuckyColor luckyColor = new LuckyColor();
            luckyColor.setAstrologer(astrologer);
            luckyColor.setLuckyDayOfWeek(day);
            luckyColor.setLuckyColorType(luckyColorType);
            luckyColor.setYear(year);

            if (!colorsStr.isEmpty()) {
                String[] colorIds = colorsStr.split(",");
                for (String colorIdStr : colorIds) {
                    Integer colorId = Integer.parseInt(colorIdStr.trim());
                    Optional<ColorCategory> colorCategoryOptional = colorCategoryService.findById(colorId);

                    if (colorCategoryOptional.isPresent()) {
                        LuckyListColor item = new LuckyListColor();
                        item.setLuckyColor(luckyColor);
                        item.setColorCategory(colorCategoryOptional.get());
                        luckyColor.getLuckyListColors().add(item);
                    }
                }

                saveOrUpdateLuckyColor(luckyColor);
            }
        }
    }

}
