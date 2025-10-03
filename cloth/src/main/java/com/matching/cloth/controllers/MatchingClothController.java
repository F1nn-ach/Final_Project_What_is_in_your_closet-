package com.matching.cloth.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matching.cloth.models.Astrologer;
import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.ColorTheory;
import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyColorItem;
import com.matching.cloth.models.LuckyColorType;
import com.matching.cloth.models.LuckyListColor;
import com.matching.cloth.models.User;
import com.matching.cloth.service.AstrologerService;
import com.matching.cloth.service.ClothService;
import com.matching.cloth.service.LuckyColorService;
import com.matching.cloth.service.LuckyColorTypeService;
import com.matching.cloth.services.ColorTheoryService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/matching")
public class MatchingClothController {

    @Autowired
    private ClothService clothService;

    @Autowired
    private ApiManagementController apiManagementController;

    @Autowired
    private AstrologerService astrologerService;

    @Autowired
    private LuckyColorService luckyColorService;

    @Autowired
    private LuckyColorTypeService luckyColorTypeService;

    @Autowired
    private ColorTheoryService colorTheoryService;

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

                List<String> hexColors = lc.getLuckyColorItems().stream()
                        .map(item -> item.getLuckyListColor().getLuckyListHex())
                        .collect(Collectors.toList());

                // Assuming กาลกิณี is bad luck, others are good luck
                if ("กาลกินี".equals(luckyColorType.getLuckyColorTypeName())) {
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
                Map<String, Object> pythonResponse = apiManagementController.callMatchColorsApi(request);
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
            Clothing mainCloth = clothService.findClothById(mainClothId);
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

            // Get all user's clothes
            List<Clothing> allUserClothes = clothService.findClothesByUser(user);

            // Build main_item object
            Map<String, Object> mainItem = new HashMap<>();
            mainItem.put("clothId", mainCloth.getClothId());
            mainItem.put("clothTypeName", mainCloth.getClothType().getTypeName());
            mainItem.put("subHex", mainCloth.getSubColorCategory().getSubHex());

            // Determine candidate lists based on main item type
            String mainTypeName = mainCloth.getClothType().getTypeName();
            List<Map<String, Object>> pantsCandidates = new ArrayList<>();
            List<Map<String, Object>> topsCandidates = new ArrayList<>();
            List<Map<String, Object>> outerCandidates = new ArrayList<>();

            if ("เสื้อ".equals(mainTypeName) || "เสื้อคลุม".equals(mainTypeName)) {
                // Main is top, get pants/skirts candidates
                pantsCandidates = allUserClothes.stream()
                        .filter(c -> "กางเกง".equals(c.getClothType().getTypeName())
                                || "กระโปรง".equals(c.getClothType().getTypeName()))
                        .map(this::clothToMap)
                        .collect(Collectors.toList());
            } else if ("กางเกง".equals(mainTypeName) || "กระโปรง".equals(mainTypeName)) {
                // Main is bottom, get tops candidates
                topsCandidates = allUserClothes.stream()
                        .filter(c -> "เสื้อ".equals(c.getClothType().getTypeName()))
                        .map(this::clothToMap)
                        .collect(Collectors.toList());
            }

            // Get outer candidates if requested
            if (hasOuter) {
                outerCandidates = allUserClothes.stream()
                        .filter(c -> "เสื้อคลุม".equals(c.getClothType().getTypeName()))
                        .map(this::clothToMap)
                        .collect(Collectors.toList());
            }

            // Get lucky colors
            Map<String, List<String>> luckyColors = new HashMap<>();
            List<String> goodLucky = new ArrayList<>();
            List<String> badLucky = new ArrayList<>();

            if (astrologerId != null && dayOfWeek != null) {
                Optional<Astrologer> astrologer = astrologerService.findAstrologerById(astrologerId);
                if (astrologer != null) {
                    List<LuckyColor> luckyColorList = luckyColorService.findByAstrologerAndDay(astrologer.get(),
                            dayOfWeek);

                    for (LuckyColor lc : luckyColorList) {
                        LuckyColorType luckyColorType = lc.getLuckyColorType();

                        // Filter by selected lucky color types if provided
                        if (luckyColorTypeIds != null && !luckyColorTypeIds.isEmpty()) {
                            if (!luckyColorTypeIds.contains(luckyColorType.getLuckyColorTypeId())) {
                                continue;
                            }
                        }

                        List<String> hexColors = lc.getLuckyColorItems().stream()
                                .map(item -> item.getLuckyListColor().getLuckyListHex())
                                .collect(Collectors.toList());

                        // Assuming กาลกิณี is bad luck, others are good luck
                        if ("กาลกิณี".equals(luckyColorType.getLuckyColorTypeName())) {
                            badLucky.addAll(hexColors);
                        } else {
                            goodLucky.addAll(hexColors);
                        }
                    }
                }
            }

            luckyColors.put("good", goodLucky);
            luckyColors.put("bad", badLucky);

            // Build request for Python API
            Map<String, Object> pythonRequest = new HashMap<>();
            pythonRequest.put("main_item", mainItem);
            pythonRequest.put("theory", colorTheory.getColorTheoryName().toLowerCase());
            pythonRequest.put("lucky_colors", luckyColors);
            pythonRequest.put("has_outer", hasOuter);
            pythonRequest.put("pants_candidates", pantsCandidates);
            pythonRequest.put("tops_candidates", topsCandidates);
            pythonRequest.put("outer_candidates", outerCandidates);

            // Call Python API
            Map<String, Object> pythonResponse = apiManagementController.callMatchColorsApi(pythonRequest);

            // Enrich response with full clothing data
            Map<String, Object> enrichedResponse = enrichMatchingResults(pythonResponse, allUserClothes);

            return ResponseEntity.ok(enrichedResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error matching clothes: " + e.getMessage()));
        }
    }

    private Map<String, Object> clothToMap(Clothing cloth) {
        Map<String, Object> map = new HashMap<>();
        map.put("clothId", cloth.getClothId());
        map.put("clothTypeName", cloth.getClothType().getTypeName());
        map.put("subHex", cloth.getSubColorCategory().getSubHex());
        return map;
    }

    private Map<String, Object> enrichMatchingResults(Map<String, Object> pythonResponse, List<Clothing> allClothes) {
        Map<String, Object> enriched = new HashMap<>();

        // Get the options from Python response
        List<Map<String, Object>> pantsOptions = (List<Map<String, Object>>) pythonResponse.get("pants_options");
        List<Map<String, Object>> topsOptions = (List<Map<String, Object>>) pythonResponse.get("tops_options");
        List<Map<String, Object>> outerOptions = (List<Map<String, Object>>) pythonResponse.get("outer_options");

        // Enrich with full clothing data
        enriched.put("pants_options", enrichClothingList(pantsOptions, allClothes));
        enriched.put("tops_options", enrichClothingList(topsOptions, allClothes));
        enriched.put("outer_options", enrichClothingList(outerOptions, allClothes));
        enriched.put("blocked_items", pythonResponse.get("blocked_items"));
        enriched.put("main_item_id", pythonResponse.get("main_item_id"));

        return enriched;
    }

    private List<Map<String, Object>> enrichClothingList(List<Map<String, Object>> options, List<Clothing> allClothes) {
        if (options == null) {
            return new ArrayList<>();
        }

        return options.stream().map(option -> {
            Integer clothId = (Integer) option.get("clothId");
            Clothing cloth = allClothes.stream()
                    .filter(c -> c.getClothId() == clothId)
                    .findFirst()
                    .orElse(null);

            Map<String, Object> enriched = new HashMap<>(option);
            if (cloth != null) {
                enriched.put("clothImage", cloth.getClothImage());
                enriched.put("username", cloth.getUser().getUsername());
                enriched.put("typeName", cloth.getClothType().getTypeName());
            }
            return enriched;
        }).collect(Collectors.toList());
    }
}
