package com.matching.cloth.controllers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.cloth.models.ClothingType;
import com.matching.cloth.models.User;
import com.matching.cloth.service.ClothingService;
import com.matching.cloth.service.ClothingTypeService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cloth")
public class AddClothController {

    @Autowired
    private ClothingService clothingService;

    @Autowired
    private ClothingTypeService clothingTypeService;

    @Value("${image.base-url}")
    private String imageBaseUrl;

    @Value("${python.api.base-url:http://127.0.0.1:5000}")
    private String pythonApiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AddClothController() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters()
                .stream()
                .filter(converter -> converter instanceof org.springframework.http.converter.StringHttpMessageConverter)
                .forEach(converter -> ((org.springframework.http.converter.StringHttpMessageConverter) converter)
                        .setDefaultCharset(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Display add cloth page
     */
    @GetMapping("/add")
    public ModelAndView showAddClothPage(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("add-cloth");
        User user = (User) session.getAttribute("user");

        if (user == null) {
            modelAndView.setViewName("redirect:/user/login");
            return modelAndView;
        }

        List<ClothingType> clothingTypes = clothingTypeService.findAllClothingTypes();
        modelAndView.addObject("clothingTypes", clothingTypes);
        modelAndView.addObject("username", user.getUsername());
        modelAndView.addObject("imageBaseUrl", imageBaseUrl);
        return modelAndView;
    }

    /**
     * Legacy URL: /add-item
     * Redirect to /cloth/add for backward compatibility
     */
    @GetMapping("/add-item")
    public String redirectAddItem() {
        return "redirect:/cloth/add";
    }

    /**
     * Add cloth item to user's closet
     */
    @PostMapping("/add")
    public ModelAndView doAddCloth(
            @RequestParam("imageFile") String imageFile,
            @RequestParam("clothingType") int clothTypeId,
            @RequestParam("classifiedColor") String classifiedColorJson,
            @RequestParam("username") String username,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            clothingService.saveCloth(username, clothTypeId, imageFile, classifiedColorJson);
            redirectAttributes.addFlashAttribute("successMessage", "เพิ่มเสื้อผ้าสำเร็จ!");
            return new ModelAndView("redirect:/cloth/add");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return new ModelAndView("redirect:/cloth/add");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการเพิ่มเสื้อผ้า");
            return new ModelAndView("redirect:/cloth/add");
        }
    }

    /**
     * API: Analyze image (remove background and classify color in one call)
     * Calls Python Flask API for background removal and color classification
     */
    @PostMapping("/api/analyze-image")
    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> analyzeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("username") String username) {

        try {
            // Step 1: Remove background
            String removeBackgroundUrl = pythonApiBaseUrl + "/cloth/remove-background";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            body.add("username", username);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> removeBackgroundResponse = restTemplate.postForEntity(removeBackgroundUrl, requestEntity, Map.class);
            Map<String, Object> removeBackgroundResult = removeBackgroundResponse.getBody();

            if (removeBackgroundResult == null || !removeBackgroundResult.containsKey("imagePath")) {
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Failed to remove background")));
            }

            String imagePath = (String) removeBackgroundResult.get("imagePath");
            String imageUrl = imageBaseUrl + username + "/" + imagePath;

            // Step 2: Classify color
            String classifyColorUrl = pythonApiBaseUrl + "/cloth/classify-color";

            HttpHeaders classifyHeaders = new HttpHeaders();
            classifyHeaders.setContentType(MediaType.APPLICATION_JSON);
            classifyHeaders.set("Accept-Charset", "UTF-8");

            Map<String, String> classifyRequest = Map.of("imageUrl", imageUrl);
            HttpEntity<Map<String, String>> classifyRequestEntity = new HttpEntity<>(classifyRequest, classifyHeaders);

            ResponseEntity<String> classifyResponse = restTemplate.postForEntity(classifyColorUrl, classifyRequestEntity, String.class);

            String classifyResponseBody = classifyResponse.getBody();
            if (classifyResponseBody == null || classifyResponseBody.isEmpty()) {
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Empty response body from Python classification service.")));
            }

            List<Map<String, Object>> colorsList = objectMapper.readValue(
                    classifyResponseBody,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                    });

            // Combine results
            Map<String, Object> combinedResult = Map.of(
                    "success", true,
                    "imagePath", imagePath,
                    "imageUrl", imageUrl,
                    "colors", colorsList
            );

            return CompletableFuture.completedFuture(ResponseEntity.ok(combinedResult));

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error analyzing image: " + e.getMessage())));
        }
    }
}
