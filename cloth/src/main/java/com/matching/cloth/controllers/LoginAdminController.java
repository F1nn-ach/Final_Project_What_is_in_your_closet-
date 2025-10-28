package com.matching.cloth.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.matching.cloth.models.Admin;
import com.matching.cloth.models.Astrologer;
import com.matching.cloth.models.ColorCategory;
import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyColorType;
import com.matching.cloth.service.AstrologerService;
import com.matching.cloth.service.ColorCategoryService;
import com.matching.cloth.service.LuckyColorService;
import com.matching.cloth.service.LuckyColorTypeService;
import com.matching.cloth.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for Admin Login and Dashboard functionality
 * Handles admin authentication and dashboard display
 */
@Controller
@RequestMapping("/admin")
public class LoginAdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AstrologerService astrologerService;

    @Autowired
    private LuckyColorService luckyColorService;

    @Autowired
    private LuckyColorTypeService luckyColorTypeService;

    @Autowired
    private ColorCategoryService colorCategoryService;

    /**
     * Process admin login
     * Handles admin authentication and session management
     */
    @PostMapping("/login")
    public String doLogin(
            HttpSession session,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model) {

        try {
            Admin admin = userService.authenticateAdmin(username, password);

            if (admin != null) {
                // Set admin session
                session.setAttribute("admin", admin);
                session.setMaxInactiveInterval(15 * 60); // 15 minutes
                session.setAttribute("userType", "admin");

                redirectAttributes.addFlashAttribute("successMessage", "เข้าสู่ระบบสำเร็จ");
                return "redirect:/admin/dashboard";
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
     * Display admin dashboard
     */
    @GetMapping("/dashboard")
    public ModelAndView getAdminDashboard(
            HttpSession session,
            @RequestParam(value = "year", required = false) Integer year) {

        ModelAndView modelAndView = new ModelAndView("admin_dashboard");
        Admin admin = (Admin) session.getAttribute("admin");

        if (admin == null) {
            modelAndView.setViewName("redirect:/user/login");
            return modelAndView;
        }

        int currentYear = (year != null) ? year : LocalDate.now().getYear();

        List<Astrologer> astrologers = astrologerService.findAllAstrologer();
        List<LuckyColor> allLuckyColors = luckyColorService.findAllLuckyColors();

        String[] days = { "อาทิตย์", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์" };
        List<LuckyColorType> luckyColorTypes = luckyColorTypeService.findAllLuckyColorTypes();
        List<ColorCategory> colorCategories = colorCategoryService.findAllColorCategories();

        modelAndView.addObject("currentYear", currentYear);
        modelAndView.addObject("astrologers", astrologers);
        modelAndView.addObject("luckyColors", allLuckyColors);
        modelAndView.addObject("daysOfWeek", days);
        modelAndView.addObject("luckyColorTypes", luckyColorTypes);
        modelAndView.addObject("colorCategories", colorCategories);

        return modelAndView;
    }

    /**
     * Logout admin
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
