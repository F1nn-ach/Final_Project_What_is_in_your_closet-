package com.matching.cloth.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.User;
import com.matching.cloth.service.ClothService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ClothController {

    @Autowired
    private ClothService clothService;

    @PostMapping("/add-item")
    public ModelAndView addCloth(
            @RequestParam("imageFile") String imageFile,
            @RequestParam("clothType") int clothTypeId,
            @RequestParam("classifiedColor") String classifiedColorJson,
            @RequestParam("username") String username,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            clothService.saveCloth(username, clothTypeId, imageFile, classifiedColorJson);
            redirectAttributes.addFlashAttribute("successMessage", "เพิ่มเสื้อผ้าสำเร็จ!");
            return new ModelAndView("redirect:/add-item");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return new ModelAndView("redirect:/add-item");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการเพิ่มเสื้อผ้า");
            return new ModelAndView("redirect:/add-item");
        }
    }

    @GetMapping("/api/filter-clothes")
    public ModelAndView filterClothes(
            @RequestParam(required = false) String mainColor,
            @RequestParam(required = false) String subColor,
            @RequestParam(required = false) Integer clothTypeId,
            HttpSession session) {

        ModelAndView modelAndView = new ModelAndView("clothes-list-fragment");

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                modelAndView.setViewName("redirect:/user-login");
                return modelAndView;
            }

            List<Clothing> filteredClothes = clothService.findByUserIdAndFilters(
                    user.getUsername(), mainColor, subColor, clothTypeId);

            modelAndView.addObject("clothes", filteredClothes);

        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "เกิดข้อผิดพลาดในการกรองข้อมูล");
        }

        return modelAndView;
    }

    @PostMapping("/delete-item")
    public ModelAndView deleteCloth(
            @RequestParam("clothId") int clothId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "กรุณาเข้าสู่ระบบ");
                return new ModelAndView("redirect:/user-login");
            }

            // ตรวจสอบว่าเสื้อผ้านี้เป็นของ user นี้หรือไม่
            Clothing cloth = clothService.findClothById(clothId);
            if (cloth == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบเสื้อผ้าที่ต้องการลบ");
                return new ModelAndView("redirect:/list-item");
            }

            if (!cloth.getUser().getUsername().equals(user.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMessage", "คุณไม่มีสิทธิ์ลบเสื้อผ้านี้");
                return new ModelAndView("redirect:/list-item");
            }

            // ลบเสื้อผ้าและสีที่ไม่ได้ใช้แล้ว
            clothService.deleteClothAndCleanupColors(clothId);
            redirectAttributes.addFlashAttribute("successMessage", "ลบเสื้อผ้าสำเร็จ!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการลบเสื้อผ้า");
        }

        return new ModelAndView("redirect:/list-item");
    }
}