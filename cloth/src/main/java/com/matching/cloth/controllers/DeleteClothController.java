package com.matching.cloth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.matching.cloth.models.User;
import com.matching.cloth.service.ClothingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for Delete Cloth functionality
 * Handles deletion of clothing items from user's closet
 */
@Controller
@RequestMapping("/cloth")
public class DeleteClothController {

    @Autowired
    private ClothingService clothingService;

    /**
     * Delete cloth item
     * Validates user ownership before deletion
     */
    @PostMapping("/delete")
    public ModelAndView doDeleteCloth(
            @RequestParam("clothId") int clothId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "กรุณาเข้าสู่ระบบ");
                return new ModelAndView("redirect:/user/login");
            }

            // Call service method that handles validation and deletion
            clothingService.deleteClothByUser(clothId, user.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "ลบเสื้อผ้าสำเร็จ!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการลบเสื้อผ้า");
        }

        return new ModelAndView("redirect:/cloth/list");
    }

    /**
     * Legacy URL: /delete-item
     * Forward POST requests to /cloth/delete for backward compatibility
     */
    @PostMapping("/delete-item")
    public String redirectDeleteItem(HttpServletRequest request) {
        return "forward:/cloth/delete";
    }
}
