package com.matching.cloth.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.ClothingType;
import com.matching.cloth.models.ColorCategory;
import com.matching.cloth.models.User;
import com.matching.cloth.service.ClothingService;
import com.matching.cloth.service.ClothingTypeService;
import com.matching.cloth.service.ColorCategoryService;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for List Clothes functionality
 * Handles displaying user's clothing collection
 */
@Controller
@RequestMapping("/cloth")
public class ListClothesController {

    @Autowired
    private ClothingService clothingService;

    @Autowired
    private ClothingTypeService clothingTypeService;

    @Autowired
    private ColorCategoryService colorCategoryService;

    @Value("${image.base-url}")
    private String imageBaseUrl;

    /**
     * Calculate luminance from hex color for sorting
     */
    private double calculateLuminance(String hexColor) {
        try {
            String hex = hexColor.replace("#", "");
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            return (0.299 * r + 0.587 * g + 0.114 * b);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Display list of user's clothes
     */
    @GetMapping("/list")
    public ModelAndView doListClothes(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("list-clothes");

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                modelAndView.setViewName("redirect:/user/login");
                return modelAndView;
            }

            List<Clothing> clothes = clothingService.findClothesByUser(user);
            List<ClothingType> clothingTypes = clothingTypeService.findAllClothingTypes();
            List<ColorCategory> colorCategories = colorCategoryService.findAllColorCategories();

            // Sort colors from dark to light based on luminance
            colorCategories.sort((c1, c2) -> {
                double lum1 = calculateLuminance(c1.getColorCategoryHex());
                double lum2 = calculateLuminance(c2.getColorCategoryHex());
                return Double.compare(lum1, lum2);
            });

            modelAndView.addObject("clothes", clothes);
            modelAndView.addObject("clothingTypes", clothingTypes);
            modelAndView.addObject("colorCategories", colorCategories);
            modelAndView.addObject("imageBaseUrl", imageBaseUrl);

        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "เกิดข้อผิดพลาดในการโหลดรายการเสื้อผ้า");
        }

        return modelAndView;
    }

    /**
     * Legacy URL: /list-item
     * Redirect to /cloth/list for backward compatibility
     */
    @GetMapping("/list-item")
    public String redirectListItem() {
        return "redirect:/cloth/list";
    }
}
