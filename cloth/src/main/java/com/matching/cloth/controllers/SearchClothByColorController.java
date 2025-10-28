package com.matching.cloth.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.User;
import com.matching.cloth.service.ClothingService;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for Search/Filter Cloth by Color functionality
 * Handles filtering clothes by color, type, and category
 */
@RestController
@RequestMapping("/api/cloth")
public class SearchClothByColorController {

    @Autowired
    private ClothingService clothingService;

    @Value("${image.base-url}")
    private String imageBaseUrl;

    /**
     * Filter clothes by color, type, and category
     */
    @PostMapping("/filter")
    public ResponseEntity<Map<String, Object>> doFilterClothes(
            @RequestParam(required = false) Integer colorId,
            @RequestParam(required = false) List<Integer> typeIds,
            @RequestParam(required = false) String category,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "กรุณาเข้าสู่ระบบ");
                return ResponseEntity.status(401).body(response);
            }

            List<Clothing> allClothes = clothingService.findClothesByUser(user);
            List<Clothing> filteredClothes = filterClothesLogic(allClothes, colorId, typeIds, category);

            List<Map<String, Object>> clothesDTOs = filteredClothes.stream()
                    .map(cloth -> convertToDTO(cloth, user.getUsername()))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("clothes", clothesDTOs);
            response.put("totalCount", clothesDTOs.size());

            Map<String, Integer> categoryCounts = new HashMap<>();
            categoryCounts.put("upper", (int) filteredClothes.stream()
                    .filter(c -> isUpperCategory(c.getClothingType().getClothingTypeName()))
                    .count());
            categoryCounts.put("lower", (int) filteredClothes.stream()
                    .filter(c -> isLowerCategory(c.getClothingType().getClothingTypeName()))
                    .count());
            response.put("categoryCounts", categoryCounts);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "เกิดข้อผิดพลาดในการกรองข้อมูล: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get available colors based on current filters
     */
    @PostMapping("/available-colors")
    public ResponseEntity<Map<String, Object>> getAvailableColors(
            @RequestParam(required = false) List<Integer> typeIds,
            @RequestParam(required = false) String category,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "กรุณาเข้าสู่ระบบ");
                return ResponseEntity.status(401).body(response);
            }

            List<Clothing> allClothes = clothingService.findClothesByUser(user);
            List<Clothing> filteredClothes = filterClothesLogic(allClothes, null, typeIds, category);

            List<Integer> availableColorIds = filteredClothes.stream()
                    .filter(cloth -> cloth.getDominantColor() != null)
                    .map(cloth -> cloth.getDominantColor().getColorCategoryId())
                    .distinct()
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("availableColorIds", availableColorIds);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "เกิดข้อผิดพลาด: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get available types based on current filters
     */
    @PostMapping("/available-types")
    public ResponseEntity<Map<String, Object>> getAvailableTypes(
            @RequestParam(required = false) Integer colorId,
            @RequestParam(required = false) String category,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "กรุณาเข้าสู่ระบบ");
                return ResponseEntity.status(401).body(response);
            }

            List<Clothing> allClothes = clothingService.findClothesByUser(user);
            List<Clothing> filteredClothes = filterClothesLogic(allClothes, colorId, null, category);

            List<Integer> availableTypeIds = filteredClothes.stream()
                    .map(cloth -> cloth.getClothingType().getClothingTypeId())
                    .distinct()
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("availableTypeIds", availableTypeIds);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "เกิดข้อผิดพลาด: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Filter clothes logic
     */
    private List<Clothing> filterClothesLogic(
            List<Clothing> clothes,
            Integer colorId,
            List<Integer> typeIds,
            String category) {

        List<Clothing> filtered = new ArrayList<>(clothes);

        if (colorId != null) {
            filtered = filtered.stream()
                    .filter(cloth -> cloth.getDominantColor() != null
                            && cloth.getDominantColor().getColorCategoryId() == colorId)
                    .collect(Collectors.toList());
        }

        if (typeIds != null && !typeIds.isEmpty()) {
            filtered = filtered.stream()
                    .filter(cloth -> typeIds.contains(cloth.getClothingType().getClothingTypeId()))
                    .collect(Collectors.toList());
        }

        if (category != null) {
            if ("upper".equals(category)) {
                filtered = filtered.stream()
                        .filter(cloth -> isUpperCategory(cloth.getClothingType().getClothingTypeName()))
                        .collect(Collectors.toList());
            } else if ("lower".equals(category)) {
                filtered = filtered.stream()
                        .filter(cloth -> isLowerCategory(cloth.getClothingType().getClothingTypeName()))
                        .collect(Collectors.toList());
            }
        }

        return filtered;
    }

    /**
     * Check if clothing type is upper category
     */
    private boolean isUpperCategory(String typeName) {
        return "เสื้อ".equals(typeName) || "เสื้อคลุม".equals(typeName);
    }

    /**
     * Check if clothing type is lower category
     */
    private boolean isLowerCategory(String typeName) {
        return "กางเกง".equals(typeName) || "กระโปรง".equals(typeName);
    }

    /**
     * Convert Clothing entity to DTO
     */
    private Map<String, Object> convertToDTO(Clothing cloth, String username) {
        Map<String, Object> dto = new HashMap<>();

        dto.put("clothId", cloth.getClothingId());

        String clothingImageFilename = cloth.getClothingImage();

        String imagePath = imageBaseUrl;
        if (!imagePath.endsWith("/")) {
            imagePath += "/";
        }
        imagePath += username + "/" + clothingImageFilename;

        dto.put("clothImage", imagePath);
        dto.put("typeName", cloth.getClothingType().getClothingTypeName());
        dto.put("typeId", cloth.getClothingType().getClothingTypeId());

        if (cloth.getDominantColor() != null) {
            dto.put("dominantColorId", cloth.getDominantColor().getColorCategoryId());
            dto.put("dominantColorName", cloth.getDominantColor().getColorCategoryName());
            dto.put("dominantColorHex", cloth.getDominantColor().getColorCategoryHex());
        } else {
            dto.put("dominantColorId", null);
            dto.put("dominantColorName", null);
            dto.put("dominantColorHex", null);
        }

        return dto;
    }
}
