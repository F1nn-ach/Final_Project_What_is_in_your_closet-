package com.matching.cloth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.matching.cloth.models.User;
import com.matching.cloth.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for User Login functionality
 * Handles user authentication and session management
 */
@Controller
@RequestMapping("/user")
public class LoginUserController {

    @Autowired
    private UserService userService;

    /**
     * Display login page
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    /**
     * Legacy URL: /user-login
     * Redirect to login page for backward compatibility
     */
    @GetMapping("/user-login")
    public String redirectUserLogin() {
        return "login";
    }

    /**
     * Legacy URL: /login-check
     * Forward POST requests to /user/login for backward compatibility
     */
    @PostMapping("/login-check")
    public String redirectLoginCheck() {
        return "forward:/user/login";
    }

    /**
     * Process user/admin login
     * Handles both user and admin authentication, session creation, and remember me
     * functionality
     */
    @PostMapping("/login")
    public String doLoginUser(
            HttpSession session,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(name = "rememberMe", required = false, defaultValue = "false") boolean rememberMe,
            Model model) {

        try {
            // Try admin authentication first
            com.matching.cloth.models.Admin admin = userService.authenticateAdmin(username, password);

            if (admin != null) {
                // Set admin session
                session.setAttribute("admin", admin);
                session.setMaxInactiveInterval(15 * 60); // 15 minutes
                session.setAttribute("userType", "admin");

                redirectAttributes.addFlashAttribute("successMessage", "เข้าสู่ระบบสำเร็จ");
                return "redirect:/admin/dashboard";
            }

            // If not admin, try user authentication
            User user = userService.authenticateUser(username, password);

            if (user != null) {
                // Set user session
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(15 * 60); // 15 minutes
                session.setAttribute("userType", "user");

                // Handle remember me functionality
                if (rememberMe) {
                    String cookieValue = userService.createRememberMeCookie(username, password);
                    Cookie rememberMeCookie = new Cookie("rememberMe", cookieValue);
                    rememberMeCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                    response.addCookie(rememberMeCookie);
                }

                redirectAttributes.addFlashAttribute("successMessage",
                        "เข้าสู่ระบบสำเร็จ ยินดีต้อนรับคุณ " + user.getUsername());
                return "redirect:/cloth/list";
            }

            model.addAttribute("error", "ชื่อผู้ใช้ หรือ รหัสผ่านไม่ถูกต้อง");
            return "login";

        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("error", "เกิดข้อผิดพลาด: " + ex.getMessage());
            return "login";
        }
    }

    /**
     * Logout user
     * Invalidate session and remove remember me cookie
     */
    @GetMapping("/logout")
    public String doLogout(HttpSession session, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        session.invalidate();

        // Remove remember me cookie
        Cookie rememberMeCookie = new Cookie("rememberMe", null);
        rememberMeCookie.setMaxAge(0);
        response.addCookie(rememberMeCookie);

        redirectAttributes.addFlashAttribute("successMessage", "ออกจากระบบสำเร็จ");
        return "redirect:/";
    }

}
