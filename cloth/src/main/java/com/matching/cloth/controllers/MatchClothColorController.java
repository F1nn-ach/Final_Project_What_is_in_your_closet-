package com.matching.cloth.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.cloth.models.Astrologer;
import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.ClothingType;
import com.matching.cloth.models.ColorCategory;
import com.matching.cloth.models.ColorTheory;
import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyColorType;
import com.matching.cloth.models.User;
import com.matching.cloth.service.AstrologerService;
import com.matching.cloth.service.ClothingService;
import com.matching.cloth.service.ClothingTypeService;
import com.matching.cloth.service.ColorCategoryService;
import com.matching.cloth.service.ColorTheoryService;
import com.matching.cloth.service.LuckyColorService;
import com.matching.cloth.service.LuckyColorTypeService;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for Match Cloth Color functionality
 * Handles color matching logic using color theory and lucky colors
 */
@RestController
@RequestMapping("/api/matching")
public class MatchClothColorController {

    @Autowired
    private ClothingService clothingService;

    @Autowired
    private AstrologerService astrologerService;

    @Autowired
    private LuckyColorService luckyColorService;

    @Autowired
    private ColorTheoryService colorTheoryService;

    @Autowired
    private ClothingTypeService clothingTypeService;

    @Autowired
    private ColorCategoryService colorCategoryService;

    @Autowired
    private LuckyColorTypeService luckyColorTypeService;

    @Value("${python.api.base-url:http://127.0.0.1:5000}")
    private String pythonApiBaseUrl;

    @Value("${image.base-url}")
    private String imageBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MatchClothColorController() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters()
                .stream()
                .filter(converter -> converter instanceof org.springframework.http.converter.StringHttpMessageConverter)
                .forEach(converter -> ((org.springframework.http.converter.StringHttpMessageConverter) converter)
                        .setDefaultCharset(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Display matching page
     */
    @GetMapping("/page")
    public ModelAndView getMatchingPage(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("matching-cloth");
        User user = (User) session.getAttribute("user");

        if (user == null) {
            modelAndView.setViewName("redirect:/user/login");
            return modelAndView;
        }

        try {
            List<Clothing> clothes = clothingService.findClothesByUser(user);
            List<ClothingType> clothingTypes = clothingTypeService.findAllClothingTypes();
            List<ColorCategory> colorCategories = colorCategoryService.findAllColorCategories();
            List<Astrologer> astrologers = astrologerService.findAllAstrologer();

            List<LuckyColorType> luckyColorTypes = luckyColorTypeService.findAllLuckyColorTypes()
                    .stream()
                    .filter(type -> !"กาลกิณี".equals(type.getLuckyColorTypeName()))
                    .toList();

            String[] days = { "อาทิตย์", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์" };
            LocalDate today = LocalDate.now();
            String currentDay = days[today.getDayOfWeek().getValue() % 7];

            List<ColorTheory> colorTheories = colorTheoryService.getAllColorTheories();
            List<LuckyColor> allLuckyColors = luckyColorService.findAllLuckyColors();

            modelAndView.addObject("clothes", clothes);
            modelAndView.addObject("clothingTypes", clothingTypes);
            modelAndView.addObject("colorCategories", colorCategories);
            modelAndView.addObject("astrologers", astrologers);
            modelAndView.addObject("luckyColorTypes", luckyColorTypes);
            modelAndView.addObject("daysOfWeek", days);
            modelAndView.addObject("currentDay", currentDay);
            modelAndView.addObject("colorTheories", colorTheories);
            modelAndView.addObject("imageBaseUrl", imageBaseUrl);
            modelAndView.addObject("allLuckyColors", allLuckyColors);

        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "เกิดข้อผิดพลาดในการโหลดข้อมูล");
        }

        return modelAndView;
    }

    /**
     * Legacy URL: /matching-item
     * Redirect to /api/matching/page for backward compatibility
     */
    @GetMapping("/matching-item")
    public String redirectMatchingItem() {
        return "redirect:/api/matching/page";
    }

    /**
     * Get lucky colors based on astrologer and day
     */
    @PostMapping("/lucky-colors")
    public ResponseEntity<?> getLuckyColors(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            Integer astrologerId = (Integer) request.get("astrologerId");
            String dayOfWeek = (String) request.get("dayOfWeek");
            List<Integer> luckyColorTypeIds = (List<Integer>) request.get("luckyColorTypeIds");

            if (astrologerId == null || dayOfWeek == null) {
                return ResponseEntity.ok(Map.of("good", List.of(), "bad", List.of()));
            }

            Optional<Astrologer> astrologerOpt = astrologerService.findAstrologerById(astrologerId);
            if (astrologerOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of("good", List.of(), "bad", List.of()));
            }

            List<LuckyColor> luckyColorList = luckyColorService.findByAstrologerAndDay(astrologerOpt.get(), dayOfWeek);

            List<String> goodLucky = new ArrayList<>();
            List<String> badLucky = new ArrayList<>();

            for (LuckyColor lc : luckyColorList) {
                LuckyColorType luckyColorType = lc.getLuckyColorType();

                // Filter by selected lucky color types if provided
                if (luckyColorTypeIds != null && !luckyColorTypeIds.isEmpty()) {
                    if (!luckyColorTypeIds.contains(luckyColorType.getLuckyColorTypeId())) {
                        continue;
                    }
                }

                List<String> hexColors = lc.getLuckyListColors().stream()
                        .map(item -> item.getColorCategory().getColorCategoryHex())
                        .collect(Collectors.toList());

                // Assuming กาลกิณี is bad luck, others are good luck
                if ("กาลกิณี".equals(luckyColorType.getLuckyColorTypeName())) {
                    badLucky.addAll(hexColors);
                } else {
                    goodLucky.addAll(hexColors);
                }
            }

            return ResponseEntity.ok(Map.of("good", goodLucky, "bad", badLucky));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("good", List.of(), "bad", List.of()));
        }
    }

    /**
     * Match clothes based on color theory and lucky colors
     */
    @PostMapping("/match")
    public ResponseEntity<?> matchClothes(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not logged in"));
            }

            // Check if request already has the Python API format (main_item, theory, etc.)
            if (request.containsKey("main_item") && request.containsKey("theory")) {
                // Request is already in Python API format, forward directly
                Map<String, Object> pythonResponse = callMatchColorsApi(request);
                return ResponseEntity.ok(pythonResponse);
            }

            // Legacy format - convert to Python API format
            Integer mainClothId = (Integer) request.get("mainClothId");
            Integer colorTheoryId = (Integer) request.get("colorTheoryId");
            Integer astrologerId = (Integer) request.get("astrologerId");
            String dayOfWeek = (String) request.get("dayOfWeek");
            List<Integer> luckyColorTypeIds = (List<Integer>) request.get("luckyColorTypeIds");
            Boolean hasOuter = (Boolean) request.getOrDefault("hasOuter", false);

            // Validate required parameters
            if (mainClothId == null || colorTheoryId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required parameters: mainClothId or colorTheoryId"));
            }

            // Get main clothing item
            Clothing mainCloth = clothingService.findClothById(mainClothId);
            if (mainCloth == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Main clothing item not found"));
            }

            // Get color theory
            ColorTheory colorTheory = colorTheoryService.getColorTheoryById(colorTheoryId);
            if (colorTheory == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Color theory not found"));
            }

            List<Clothing> allUserClothes = clothingService.findClothesByUser(user);

            Map<String, Object> mainItem = new HashMap<>();
            mainItem.put("clothId", mainCloth.getClothingId());
            mainItem.put("clothTypeName", mainCloth.getClothingType().getClothingTypeName());

            String dominantHex = mainCloth.getDominantColor() != null
                    ? mainCloth.getDominantColor().getColorCategoryHex()
                    : "#000000";
            String dominantName = mainCloth.getDominantColor() != null
                    ? mainCloth.getDominantColor().getColorCategoryName()
                    : "";
            mainItem.put("colorHex", dominantHex);
            mainItem.put("colorName", dominantName);

            String mainTypeName = mainCloth.getClothingType().getClothingTypeName();
            List<Map<String, Object>> pantsCandidates = new ArrayList<>();
            List<Map<String, Object>> topsCandidates = new ArrayList<>();
            List<Map<String, Object>> outerCandidates = new ArrayList<>();

            if ("เสื้อ".equals(mainTypeName) || "เสื้อคลุม".equals(mainTypeName)) {
                pantsCandidates = allUserClothes.stream()
                        .filter(c -> "กางเกง".equals(c.getClothingType().getClothingTypeName())
                                || "กระโปรง".equals(c.getClothingType().getClothingTypeName()))
                        .map(this::clothToMap)
                        .collect(Collectors.toList());
            } else if ("กางเกง".equals(mainTypeName) || "กระโปรง".equals(mainTypeName)) {
                topsCandidates = allUserClothes.stream()
                        .filter(c -> "เสื้อ".equals(c.getClothingType().getClothingTypeName()))
                        .map(this::clothToMap)
                        .collect(Collectors.toList());
            }

            if (hasOuter) {
                outerCandidates = allUserClothes.stream()
                        .filter(c -> "เสื้อคลุม".equals(c.getClothingType().getClothingTypeName()))
                        .map(this::clothToMap)
                        .collect(Collectors.toList());
            }

            Map<String, List<String>> luckyColors = new HashMap<>();
            List<String> goodLucky = new ArrayList<>();
            List<String> badLucky = new ArrayList<>();

            if (astrologerId != null && dayOfWeek != null) {
                Optional<Astrologer> astrologer = astrologerService.findAstrologerById(astrologerId);
                if (astrologer.isPresent()) {
                    List<LuckyColor> luckyColorList = luckyColorService.findByAstrologerAndDay(astrologer.get(),
                            dayOfWeek);

                    for (LuckyColor lc : luckyColorList) {
                        LuckyColorType luckyColorType = lc.getLuckyColorType();

                        List<String> hexColors = lc.getLuckyListColors().stream()
                                .map(item -> item.getColorCategory().getColorCategoryHex())
                                .collect(Collectors.toList());

                        if ("กาลกินี".equals(luckyColorType.getLuckyColorTypeName())) {
                            badLucky.addAll(hexColors);
                        } else {
                            if (luckyColorTypeIds != null && !luckyColorTypeIds.isEmpty()) {
                                if (!luckyColorTypeIds.contains(luckyColorType.getLuckyColorTypeId())) {
                                    continue;
                                }
                            }
                            goodLucky.addAll(hexColors);
                        }
                    }
                }
            }

            luckyColors.put("good", goodLucky);
            luckyColors.put("bad", badLucky);

            Map<String, Object> pythonRequest = new HashMap<>();
            pythonRequest.put("main_item", mainItem);
            pythonRequest.put("theory", colorTheory.getColorTheoryName().toLowerCase());
            pythonRequest.put("lucky_colors", luckyColors);
            pythonRequest.put("has_outer", hasOuter);
            pythonRequest.put("pants_candidates", pantsCandidates);
            pythonRequest.put("tops_candidates", topsCandidates);
            pythonRequest.put("outer_candidates", outerCandidates);

            Map<String, Object> pythonResponse = callMatchColorsApi(pythonRequest);
            Map<String, Object> enrichedResponse = enrichMatchingResults(pythonResponse, allUserClothes);

            return ResponseEntity.ok(enrichedResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error matching clothes: " + e.getMessage()));
        }
    }

    /**
     * Convert Clothing to Map
     */
    private Map<String, Object> clothToMap(Clothing cloth) {
        Map<String, Object> map = new HashMap<>();
        map.put("clothId", cloth.getClothingId());
        map.put("clothTypeName", cloth.getClothingType().getClothingTypeName());

        String dominantHex = cloth.getDominantColor() != null
                ? cloth.getDominantColor().getColorCategoryHex()
                : "#000000";
        String dominantName = cloth.getDominantColor() != null
                ? cloth.getDominantColor().getColorCategoryName()
                : "";
        map.put("colorHex", dominantHex);
        map.put("colorName", dominantName);
        return map;
    }

    /**
     * Call Python Flask API to match colors
     */
    private Map<String, Object> callMatchColorsApi(Map<String, Object> requestData) {
        try {
            String url = pythonApiBaseUrl + "/cloth/match-colors";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                        });
            } else {
                throw new RuntimeException("Python API returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Python matching API: " + e.getMessage(), e);
        }
    }

    /**
     * Enrich matching results with additional clothing data
     */
    private Map<String, Object> enrichMatchingResults(Map<String, Object> pythonResponse, List<Clothing> allClothes) {
        Map<String, Object> enriched = new HashMap<>();

        List<Map<String, Object>> pantsOptions = (List<Map<String, Object>>) pythonResponse.get("pants_options");
        List<Map<String, Object>> topsOptions = (List<Map<String, Object>>) pythonResponse.get("tops_options");
        List<Map<String, Object>> outerOptions = (List<Map<String, Object>>) pythonResponse.get("outer_options");

        enriched.put("pants_options", enrichClothingList(pantsOptions, allClothes));
        enriched.put("tops_options", enrichClothingList(topsOptions, allClothes));
        enriched.put("outer_options", enrichClothingList(outerOptions, allClothes));
        enriched.put("blocked_items", pythonResponse.get("blocked_items"));
        enriched.put("main_item_id", pythonResponse.get("main_item_id"));

        return enriched;
    }

    /**
     * Enrich clothing list with additional data
     */
    private List<Map<String, Object>> enrichClothingList(List<Map<String, Object>> options, List<Clothing> allClothes) {
        if (options == null) {
            return new ArrayList<>();
        }

        return options.stream().map(option -> {
            Integer clothId = (Integer) option.get("clothId");
            Clothing cloth = allClothes.stream()
                    .filter(c -> c.getClothingId() == clothId)
                    .findFirst()
                    .orElse(null);

            Map<String, Object> enriched = new HashMap<>(option);
            if (cloth != null) {
                enriched.put("clothImage", cloth.getClothingImage());
                enriched.put("username", cloth.getUser().getUsername());
                enriched.put("typeName", cloth.getClothingType().getClothingTypeName());
            }
            return enriched;
        }).collect(Collectors.toList());
    }
}
