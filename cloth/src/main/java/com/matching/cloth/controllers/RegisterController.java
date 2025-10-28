package com.matching.cloth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.matching.cloth.models.User;
import com.matching.cloth.service.UserService;

/**
 * Controller for User Registration functionality
 * Handles user registration, username and email validation
 */
@Controller
@RequestMapping("/user")
public class RegisterController {

    @Autowired
    private UserService userService;

    /**
     * Display registration page
     */
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    /**
     * Check if username is available
     * Used for real-time validation during registration
     */
    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam String username) {
        return userService.checkUsernameAvailability(username);
    }

    /**
     * Check if email already exists
     * Used for real-time validation during registration
     */
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return userService.isEmailExists(email);
    }

    /**
     * Process user registration
     * Creates new user account with provided information
     */
    @PostMapping("/register")
    public ModelAndView doRegister(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            RedirectAttributes redirectAttributes) {

        ModelAndView modelAndView = new ModelAndView("register");

        try {
            User result = userService.registerUser(username, password, email, phone);

            if (result != null && result.getUsername().equals(username)) {
                redirectAttributes.addFlashAttribute("successMessage", "สมัครสมาชิกสำเร็จ! กรุณาเข้าสู่ระบบ");
                return new ModelAndView("redirect:/user/login");
            } else {
                modelAndView.addObject("errorMessage", "การสมัครสมาชิกล้มเหลว กรุณาลองอีกครั้ง");
                return modelAndView;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            modelAndView.addObject("errorMessage", "เกิดข้อผิดพลาด: " + ex.getMessage());
            return modelAndView;
        }
    }

}
