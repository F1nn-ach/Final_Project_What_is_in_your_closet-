package com.matching.cloth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.matching.cloth.service.LuckyColorService;

/**
 * Controller for Add Lucky Color functionality
 * Handles creation of lucky color data for astrologers
 */
@Controller
@RequestMapping("/admin/lucky-color")
public class AddLuckyColorController {

    @Autowired
    private LuckyColorService luckyColorService;

    /**
     * Save lucky colors data
     * Creates lucky color entries for specific astrologer and year
     */
    @PostMapping("/save")
    public String saveLuckyColor(
            @RequestParam("astrologerName") String astrologerName,
            @RequestParam("year") Integer year,
            @RequestParam("luckyColorsData") String luckyColorsDataJson,
            @RequestParam(value = "selectedTypes", required = false) String selectedTypesJson,
            RedirectAttributes redirectAttributes) {

        try {
            luckyColorService.saveLuckyColorsData(astrologerName, year, luckyColorsDataJson, selectedTypesJson);
            redirectAttributes.addFlashAttribute("success", "เพิ่มข้อมูลสีมงคลสำเร็จ");
            return "redirect:/admin/dashboard";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "เกิดข้อผิดพลาดในการเพิ่มข้อมูล: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }
}
