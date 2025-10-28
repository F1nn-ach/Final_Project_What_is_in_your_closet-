package com.matching.cloth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.matching.cloth.service.AstrologerService;

/**
 * Controller for Delete Astrologer functionality
 * Handles deletion of astrologer records
 */
@Controller
@RequestMapping("/admin/astrologer")
public class DeleteAstrologerController {

    @Autowired
    private AstrologerService astrologerService;

    /**
     * Delete astrologer by ID
     */
    @PostMapping("/delete/{id}")
    public String doDeleteAstrologer(@PathVariable("id") Integer id) {
        boolean deleted = astrologerService.deleteAstrologerById(id);
        if (deleted) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/admin/dashboard?error=notfound";
        }
    }
}
