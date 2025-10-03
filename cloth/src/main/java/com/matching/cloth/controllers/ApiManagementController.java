package com.matching.cloth.controllers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping("/api")
@Controller
public class ApiManagementController {

    @Value("${python.api.base-url:http://127.0.0.1:5000}")
    private String pythonApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/remove-background")
    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> removeBackground(
            @RequestParam("file") MultipartFile file, @RequestParam("username") String username) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://127.0.0.1:5000/cloth/remove-background";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("username", username);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            return CompletableFuture.completedFuture(ResponseEntity.ok(response.getBody()));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error processing image: " + e.getMessage())));
        }
    }

    @PostMapping("/classify-cloth-color")
    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> classifyClothColor(
            @RequestBody Map<String, String> request) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://127.0.0.1:5000/cloth/classify-color";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Empty response body from Python classification service.")));
            }
            return CompletableFuture.completedFuture(ResponseEntity.ok(responseBody));
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = Map.of("error", "Failed to classify cloth color: " + e.getMessage());
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errorResponse));
        }
    }

    /**
     * Call Python Flask API to match colors
     * @param requestData The matching request data
     * @return Response from Python API
     */
    public Map<String, Object> callMatchColorsApi(Map<String, Object> requestData) {
        try {
            String url = pythonApiBaseUrl + "/cloth/match-colors";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } else {
                throw new RuntimeException("Python API returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Python matching API: " + e.getMessage(), e);
        }
    }
}
