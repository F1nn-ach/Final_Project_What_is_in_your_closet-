package com.matching.cloth.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.matching.cloth.models.Astrologer;
import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyColorItem;
import com.matching.cloth.models.LuckyColorType;
import com.matching.cloth.models.LuckyListColor;
import com.matching.cloth.service.AstrologerService;
import com.matching.cloth.service.LuckyColorService;
import com.matching.cloth.service.LuckyColorTypeService;
import com.matching.cloth.service.LuckyListColorService;

@Controller
public class AdminDashbordController {

    @Autowired
    private AstrologerService astrologerService;

    @Autowired
    private LuckyColorService luckyColorService;

    @Autowired
    private LuckyColorTypeService luckyColorTypeService;

    @Autowired
    private LuckyListColorService luckyListColorService;

    @PostMapping("/delete/astrologer/{id}")
    public String deleteAstrologer(@PathVariable("id") Integer id) {
        boolean deleted = astrologerService.deleteAstrologerById(id);
        if (deleted) {
            return "redirect:/dashboard";
        } else {
            return "redirect:/dashboard?error=notfound";
        }
    }

    @PostMapping("/delete-selected-colors")
    public String deleteSelectedColors(@RequestParam("ids") List<Integer> ids, RedirectAttributes redirectAttributes) {
        boolean deleted = luckyColorService.deleteLuckyColorsByIds(ids);
        if (deleted) {
            return "redirect:/dashboard";
        } else {
            return "redirect:/dashboard?error=notfound";
        }
    }

    @PostMapping("/save-matrix-data")
    public String saveMatrixData(
            @RequestParam("astrologerName") String astrologerName,
            @RequestParam("year") Integer year,
            @RequestParam("matrixData") String matrixDataJson,
            @RequestParam(value = "selectedTypes", required = false) String selectedTypesJson,
            RedirectAttributes redirectAttributes) {

        try {
            // Parse astrologer
            Astrologer astrologer = astrologerService.findAstrologerByName(astrologerName);
            if (astrologer == null) {
                // Create new astrologer if it starts with __new__
                if (astrologerName.startsWith("__new__:")) {
                    String newName = astrologerName.substring(8);
                    astrologer = new Astrologer();
                    astrologer.setAstrologerName(newName);
                    astrologer = astrologerService.saveOrUpdateAstrologer(astrologer);
                } else {
                    redirectAttributes.addFlashAttribute("error", "ไม่พบแม่หมอ");
                    return "redirect:/dashboard";
                }
            }

            // Get selected type IDs to know which ones to clear if empty
            List<Integer> selectedTypeIds = new java.util.ArrayList<>();
            if (selectedTypesJson != null && !selectedTypesJson.isEmpty()) {
                String[] typeIds = selectedTypesJson.split(",");
                for (String typeIdStr : typeIds) {
                    selectedTypeIds.add(Integer.parseInt(typeIdStr.trim()));
                }
            }

            // First, delete all existing data for this astrologer/year for selected types
            // This ensures cleared cells are actually deleted
            String[] daysArray = { "อาทิตย์", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์" };
            for (String day : daysArray) {
                for (Integer typeId : selectedTypeIds) {
                    Optional<LuckyColorType> typeOptional = luckyColorTypeService.findByLuckyColorTypeId(typeId);
                    if (typeOptional.isPresent()) {
                        Optional<LuckyColor> existing = luckyColorService.findDuplicate(astrologer, day, typeOptional.get());
                        if (existing.isPresent() && existing.get().getYear() == year) {
                            luckyColorService.deleteLuckyColorsByIds(java.util.Arrays.asList(existing.get().getLuckyColorId()));
                        }
                    }
                }
            }

            // Parse JSON format: {"day-typeId": [colorId1, colorId2], ...}
            String[] entries = matrixDataJson.split(";");

            for (String entry : entries) {
                if (entry.trim().isEmpty()) continue;

                String[] parts = entry.split(":");
                if (parts.length != 2) continue;

                String key = parts[0].trim();
                String colorsStr = parts[1].trim();

                String[] keyParts = key.split("-");
                if (keyParts.length != 2) continue;

                String day = keyParts[0];
                Integer typeId = Integer.parseInt(keyParts[1]);

                Optional<LuckyColorType> luckyColorTypeOptional = luckyColorTypeService.findByLuckyColorTypeId(typeId);
                if (!luckyColorTypeOptional.isPresent()) continue;

                LuckyColorType luckyColorType = luckyColorTypeOptional.get();

                // Create new entry
                LuckyColor luckyColor = new LuckyColor();
                luckyColor.setAstrologer(astrologer);
                luckyColor.setLuckyDayOfWeek(day);
                luckyColor.setLuckyColorType(luckyColorType);
                luckyColor.setYear(year);

                // Parse color IDs
                if (!colorsStr.isEmpty()) {
                    String[] colorIds = colorsStr.split(",");
                    for (String colorIdStr : colorIds) {
                        Integer colorId = Integer.parseInt(colorIdStr.trim());
                        Optional<LuckyListColor> colorOptional = luckyListColorService.findByLuckyListColorId(colorId);

                        if (colorOptional.isPresent()) {
                            LuckyColorItem item = new LuckyColorItem();
                            item.setLuckyColor(luckyColor);
                            item.setLuckyListColor(colorOptional.get());
                            luckyColor.getLuckyColorItems().add(item);
                        }
                    }

                    luckyColorService.saveOrUpdateLuckyColor(luckyColor);
                }
            }

            redirectAttributes.addFlashAttribute("success", "บันทึกข้อมูลสีมงคลสำเร็จ");
            return "redirect:/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "เกิดข้อผิดพลาด: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

}