package com.matching.cloth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.ClothingType;
import com.matching.cloth.models.MainColorCategory;
import com.matching.cloth.models.SubColorCategory;
import com.matching.cloth.models.User;
import com.matching.cloth.repositories.ClothRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClothService {

    @Autowired
    private ClothRepository clothRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ClothTypeService clothTypeService;

    @Autowired
    private MainColorCategoryService mainColorCategoryService;

    @Autowired
    private SubColorCategoryService subColorCategoryService;

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
            Integer clothTypeId) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with username: " + username);
        }

        List<Clothing> userClothes = clothRepository.findByUser(userOptional.get());

        return filterClothes(userClothes, mainColor, subColor, clothTypeId);
    }

    public List<Clothing> findByUserIdAndFilters(String username, String mainColor, String subColor, Integer clothTypeId) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + username);
        }

        List<Clothing> userClothes = clothRepository.findByUser(userOptional.get());

        return filterClothes(userClothes, mainColor, subColor, clothTypeId);
    }

    private List<Clothing> filterClothes(List<Clothing> clothes, String mainColor, String subColor, Integer clothTypeId) {
        return clothes.stream()
                .filter(cloth -> {
                    // Filter by main color if specified
                    if (mainColor != null && !mainColor.trim().isEmpty()) {
                        String clothMainColor = cloth.getSubColorCategory().getMainColorCategory().getMainColorName();
                        if (!mainColor.equalsIgnoreCase(clothMainColor)) {
                            return false;
                        }
                    }

                    if (subColor != null && !subColor.trim().isEmpty()) {
                        String clothSubColor = cloth.getSubColorCategory().getSubColorName();
                        if (!subColor.equalsIgnoreCase(clothSubColor)) {
                            return false;
                        }
                    }

                    if (clothTypeId != null) {
                        Integer clothClothTypeId = cloth.getClothType().getClothTypeId();
                        if (!clothTypeId.equals(clothClothTypeId)) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    public Clothing saveCloth(String username, int clothTypeId, String filename, String classifiedColorJson) {
        Optional<User> userOptional = userService.findByUsername(username);
        Optional<ClothingType> clothTypeOptional = clothTypeService.findClothTypeById(clothTypeId);

        if (userOptional.isEmpty() || clothTypeOptional.isEmpty()) {
            throw new IllegalArgumentException("User or ClothType not found.");
        }

        User user = userOptional.get();
        ClothingType clothType = clothTypeOptional.get();


        Clothing cloth = new Clothing();
        cloth.setUser(user);
        cloth.setClothType(clothType);
        cloth.setClothImage(filename);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> classificationResultMap;
        try {
            classificationResultMap = objectMapper.readValue(classifiedColorJson,
                    new TypeReference<Map<String, Object>>() {
                    });

            Map<String, String> mainGroupColor = (Map<String, String>) classificationResultMap.get("main_group_color");
            Map<String, String> subGroupColor = (Map<String, String>) classificationResultMap.get("sub_group_color");

            if (mainGroupColor == null || subGroupColor == null) {
                throw new IllegalArgumentException(
                        "classifiedColorJson is missing 'main_group_color' or 'sub_group_color'.");
            }

            String mainColorName = mainGroupColor.get("main_group_name");
            String mainHex = mainGroupColor.get("main_hex");
            String subColorName = subGroupColor.get("sub_group_name");
            String subHex = subGroupColor.get("sub_hex");

            if (mainColorName == null || mainHex == null || subColorName == null || subHex == null) {
                throw new IllegalArgumentException(
                        "Missing color information in 'main_group_color' or 'sub_group_color'.");
            }

            MainColorCategory mainColorCategory = mainColorCategoryService.findOrCreateMainColorCategory(mainColorName,
                    mainHex);
            SubColorCategory subColorCategory = subColorCategoryService.findOrCreateSubColorCategory(subColorName,
                    subHex, mainColorCategory);

            cloth.setSubColorCategory(subColorCategory);

            List<Map<String, String>> colorsList = (List<Map<String, String>>) classificationResultMap.get("colors");
            if (colorsList != null) {
                List<String> extractedColorHexes = colorsList.stream()
                        .map(colorMap -> colorMap.get("hex"))
                        .collect(Collectors.toList());
                cloth.setColorHex(extractedColorHexes);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(
                    "Error parsing classifiedColorJson or creating color categories: " + e.getMessage(), e);
        }

        return clothRepository.save(cloth);
    }

    public Clothing findClothById(int clothId) {
        Optional<Clothing> clothOptional = clothRepository.findById(clothId);
        return clothOptional.orElse(null);
    }

    public void deleteCloth(int clothId) {
        clothRepository.deleteById(clothId);
    }

    public boolean isSubColorUsedByOtherClothes(SubColorCategory subColorCategory, int excludeClothId) {
        List<Clothing> clothesUsingThisSubColor = clothRepository.findBySubColorCategory(subColorCategory);
        return clothesUsingThisSubColor.stream()
                .anyMatch(cloth -> cloth.getClothId() != excludeClothId);
    }

    public boolean isMainColorUsedByOtherClothes(MainColorCategory mainColorCategory, int excludeClothId) {
        List<Clothing> clothesUsingThisMainColor = clothRepository
                .findBySubColorCategoryMainColorCategory(mainColorCategory);
        return clothesUsingThisMainColor.stream()
                .anyMatch(cloth -> cloth.getClothId() != excludeClothId);
    }

    public void deleteClothAndCleanupColors(int clothId) {
        // Get the cloth before deleting to check its colors
        Optional<Clothing> clothOptional = clothRepository.findById(clothId);
        if (clothOptional.isEmpty()) {
            throw new IllegalArgumentException("Cloth not found with ID: " + clothId);
        }

        Clothing cloth = clothOptional.get();
        SubColorCategory subColorCategory = cloth.getSubColorCategory();
        MainColorCategory mainColorCategory = subColorCategory != null ? subColorCategory.getMainColorCategory() : null;

        // Delete the cloth first
        clothRepository.deleteById(clothId);

        // Check if sub color is still used by other clothes
        if (subColorCategory != null && !isSubColorUsedByOtherClothes(subColorCategory, clothId)) {
            System.out.println("Deleting unused sub color: " + subColorCategory.getSubColorName());
            subColorCategoryService.deleteSubColorCategory(subColorCategory.getSubColorCategoryId());

            // Check if main color is still used by other clothes (after sub color deletion)
            if (mainColorCategory != null && !isMainColorUsedByOtherClothes(mainColorCategory, clothId)) {
                System.out.println("Deleting unused main color: " + mainColorCategory.getMainColorName());
                mainColorCategoryService.deleteMainColorCategory(mainColorCategory.getMainColorCategory());
            }
        }
    }
}