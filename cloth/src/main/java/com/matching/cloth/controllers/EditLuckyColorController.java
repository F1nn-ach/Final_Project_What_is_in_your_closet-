package com.matching.cloth.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.service.LuckyColorService;

/**
 * Controller for Edit Lucky Color functionality
 * Handles editing/updating lucky color data for astrologers
 */
@Controller
@RequestMapping("/admin/lucky-color")
public class EditLuckyColorController {

    @Autowired
    private LuckyColorService luckyColorService;

    /**
     * List all lucky colors
     * Returns all lucky color entries sorted by astrologer, day, and type
     */
    @GetMapping("/list")
    @ResponseBody
    public List<LuckyColor> listLuckyColors() {
        return luckyColorService.findAllLuckyColors();
    }

    /**
     * Edit/Update lucky colors data
     * Updates existing lucky color entries for specific astrologer and year
     */
    @PostMapping("/edit")
    public String doEditLuckyColor(
            @RequestParam("astrologerName") String astrologerName,
            @RequestParam("year") Integer year,
            @RequestParam("luckyColorsData") String luckyColorsDataJson,
            @RequestParam(value = "selectedTypes", required = false) String selectedTypesJson,
            RedirectAttributes redirectAttributes) {

        try {
            luckyColorService.editLuckyColorsData(astrologerName, year, luckyColorsDataJson, selectedTypesJson);
            redirectAttributes.addFlashAttribute("success", "แก้ไขข้อมูลสีมงคลสำเร็จ");
            return "redirect:/admin/dashboard";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "เกิดข้อผิดพลาดในการแก้ไขข้อมูล: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }
}
