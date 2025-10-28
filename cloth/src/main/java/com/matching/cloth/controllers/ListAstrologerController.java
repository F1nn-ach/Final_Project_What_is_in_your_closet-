package com.matching.cloth.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matching.cloth.models.Astrologer;
import com.matching.cloth.service.AstrologerService;

/**
 * Controller for Listing Astrologers
 * Handles retrieving all astrologers
 */
@RestController
@RequestMapping("/api/admin/astrologer")
public class ListAstrologerController {

    @Autowired
    private AstrologerService astrologerService;

    /**
     * List all astrologers
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listAstrologers() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Astrologer> astrologers = astrologerService.findAllAstrologer();

            List<Map<String, Object>> astrologerDTOs = astrologers.stream()
                    .map(astrologer -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("astrologerId", astrologer.getAstrologerId());
                        dto.put("astrologerName", astrologer.getAstrologerName());
                        return dto;
                    })
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("astrologers", astrologerDTOs);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "เกิดข้อผิดพลาดในการโหลดข้อมูลนักโหราศาสตร์: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
