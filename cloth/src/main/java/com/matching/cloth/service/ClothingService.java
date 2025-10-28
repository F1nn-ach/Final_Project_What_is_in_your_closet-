package com.matching.cloth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.ClothingColor;
import com.matching.cloth.models.ClothingType;
import com.matching.cloth.models.ColorCategory;
import com.matching.cloth.models.User;
import com.matching.cloth.repositories.ClothRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClothingService {

    @Autowired
    private ClothRepository clothRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ClothingTypeService clothingTypeService;

    @Autowired
    private ColorCategoryService colorCategoryService;

    @Autowired
    private ClothingColorService clothingColorService;

    public List<Clothing> findClothesByUser(User user) {
        return clothRepository.findByUser(user);
    }

    public List<Clothing> findByUsername(String username) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with username: " + username);
        }
        return clothRepository.findByUser(userOptional.get());
    }

    public List<Clothing> findByUsernameAndFilters(String username, String mainColor, String subColor,
            Integer clothingTypeId) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with username: " + username);
        }

        List<Clothing> userClothes = clothRepository.findByUser(userOptional.get());

        return filterClothes(userClothes, mainColor, subColor, clothingTypeId);
    }

    public List<Clothing> findByUserIdAndFilters(String username, String mainColor, String subColor,
            Integer clothingTypeId) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + username);
        }

        List<Clothing> userClothes = clothRepository.findByUser(userOptional.get());

        return filterClothes(userClothes, mainColor, subColor, clothingTypeId);
    }

    private List<Clothing> filterClothes(List<Clothing> clothes, String colorName, String subColor,
            Integer clothingTypeId) {
        return clothes.stream()
                .filter(cloth -> {
                    if (colorName != null && !colorName.trim().isEmpty()) {
                        ColorCategory dominantColor = cloth.getDominantColor();
                        if (dominantColor == null) {
                            return false;
                        }
                        String clothColorName = dominantColor.getColorCategoryName();
                        if (!colorName.equalsIgnoreCase(clothColorName)) {
                            return false;
                        }
                    }

                    if (clothingTypeId != null) {
                        Integer clothClothingTypeId = cloth.getClothingType().getClothingTypeId();
                        if (!clothingTypeId.equals(clothClothingTypeId)) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    public Clothing saveCloth(String username, int clothingTypeId, String filename, String classifiedColorJson) {
        Optional<User> userOptional = userService.findByUsername(username);
        Optional<ClothingType> clothingTypeOptional = clothingTypeService.findClothingTypeById(clothingTypeId);

        if (userOptional.isEmpty() || clothingTypeOptional.isEmpty()) {
            throw new IllegalArgumentException("User or ClothingType not found.");
        }

        User user = userOptional.get();
        ClothingType clothingType = clothingTypeOptional.get();

        Clothing cloth = new Clothing();
        cloth.setUser(user);
        cloth.setClothingType(clothingType);
        cloth.setClothingImage(filename);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> classificationResultMap;
        try {
            classificationResultMap = objectMapper.readValue(classifiedColorJson,
                    new TypeReference<Map<String, Object>>() {
                    });

            List<Map<String, Object>> colorsList = (List<Map<String, Object>>) classificationResultMap.get("colors");

            if (colorsList == null || colorsList.isEmpty()) {
                throw new IllegalArgumentException("No colors returned from API");
            }

            Clothing savedCloth = clothRepository.save(cloth);

            for (Map<String, Object> colorMap : colorsList) {
                String colorName = (String) colorMap.get("color_name");
                Number percentageNum = (Number) colorMap.get("percentage");

                if (colorName == null || percentageNum == null) {
                    System.out.println("Skipping color entry with missing data");
                    continue;
                }

                float percentage = percentageNum.floatValue();
                Optional<ColorCategory> colorCategoryOptional = colorCategoryService.findByColorCategoryName(colorName);

                if (!colorCategoryOptional.isPresent()) {
                    String trimmedName = colorName.trim();
                    if (!trimmedName.equals(colorName)) {
                        colorCategoryOptional = colorCategoryService.findByColorCategoryName(trimmedName);
                    }

                    if (!colorCategoryOptional.isPresent()) {
                        continue;
                    }
                }

                ColorCategory colorCategory = colorCategoryOptional.get();
                ClothingColor clothingColor = new ClothingColor();

                clothingColor.setClothing(savedCloth);
                clothingColor.setColorCategory(colorCategory);
                clothingColor.setPercentage(percentage);

                clothingColorService.save(clothingColor);
                savedCloth.getClothingColors().add(clothingColor);
            }

            return savedCloth;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(
                    "Error parsing classifiedColorJson or creating color categories: " + e.getMessage(), e);
        }
    }

    public Clothing findClothById(int clothId) {
        Optional<Clothing> clothOptional = clothRepository.findById(clothId);
        return clothOptional.orElse(null);
    }

    public void deleteCloth(int clothId) {
        clothRepository.deleteById(clothId);
    }

    public void deleteClothAndCleanupColors(int clothId) {
        Optional<Clothing> clothOptional = clothRepository.findById(clothId);
        if (clothOptional.isEmpty()) {
            throw new IllegalArgumentException("Cloth not found with ID: " + clothId);
        }

        clothRepository.deleteById(clothId);
    }

    public void deleteClothByUser(int clothId, String username) {
        Clothing cloth = findClothById(clothId);
        if (cloth == null) {
            throw new IllegalArgumentException("ไม่พบเสื้อผ้าที่ต้องการลบ");
        }

        if (!cloth.getUser().getUsername().equals(username)) {
            throw new SecurityException("คุณไม่มีสิทธิ์ลบเสื้อผ้านี้");
        }

        deleteClothAndCleanupColors(clothId);
    }
}