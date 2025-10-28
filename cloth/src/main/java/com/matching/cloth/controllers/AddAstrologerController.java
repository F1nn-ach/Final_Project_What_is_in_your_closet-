package com.matching.cloth.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matching.cloth.models.Astrologer;
import com.matching.cloth.service.AstrologerService;

/**
 * Controller for Adding Astrologer
 * Handles creating new astrologers
 */
@RestController
@RequestMapping("/api/admin/astrologer")
public class AddAstrologerController {

    @Autowired
    private AstrologerService astrologerService;

    /**
     * Add/Create new astrologer
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addAstrologer(
            @RequestParam("astrologerName") String astrologerName) {

        Map<String, Object> response = new HashMap<>();

        try {
            Astrologer existingAstrologer = astrologerService.findAstrologerByName(astrologerName);
            if (existingAstrologer != null) {
                response.put("success", false);
                response.put("message", "มีแม่หมอชื่อนี้อยู่ในระบบแล้ว");
                return ResponseEntity.ok(response);
            }

            Astrologer newAstrologer = new Astrologer();
            newAstrologer.setAstrologerName(astrologerName);
            Astrologer savedAstrologer = astrologerService.saveOrUpdateAstrologer(newAstrologer);

            response.put("success", true);
            response.put("astrologerId", savedAstrologer.getAstrologerId());
            response.put("astrologerName", savedAstrologer.getAstrologerName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "เกิดข้อผิดพลาด: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
