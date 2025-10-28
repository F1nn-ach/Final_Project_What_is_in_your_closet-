package com.matching.cloth.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyListColor;
import com.matching.cloth.service.LuckyColorService;

/**
 * Controller for home page
 * Displays random lucky color for today
 */
@Controller
public class HomeController {

    @Autowired
    private LuckyColorService luckyColorService;

    /**
     * Display home page with random lucky color for today
     */
    @GetMapping("/")
    public ModelAndView getHomePage() {
        ModelAndView modelAndView = new ModelAndView("home");
        LocalDate today = LocalDate.now();
        String[] days = { "อาทิตย์", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์" };
        String dayOfWeekStr = days[today.getDayOfWeek().getValue() % 7];

        List<LuckyColor> luckyColors = luckyColorService.findByDayOfWeek(dayOfWeekStr);

        if (luckyColors.isEmpty()) {
            return modelAndView;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(luckyColors.size());
        LuckyColor luckyColor = luckyColors.get(randomIndex);

        // Avoid กาลกิณี type
        if ("กาลกิณี".equals(luckyColor.getLuckyColorType().getLuckyColorTypeName())) {
            randomIndex = (randomIndex + 1) % luckyColors.size();
            luckyColor = luckyColors.get(randomIndex);
        }

        List<LuckyListColor> items = luckyColor.getLuckyListColors();
        LuckyListColor selectedItem = items.get(random.nextInt(items.size()));

        modelAndView.addObject("selectedItem", selectedItem);
        return modelAndView;
    }

    /**
     * Legacy URL: /user-login
     * Redirect to /user/login for backward compatibility
     */
    @GetMapping("/user-login")
    public String redirectUserLogin() {
        return "redirect:/user/login";
    }

    /**
     * Legacy URL: /logout
     * Redirect to /user/logout for backward compatibility
     */
    @GetMapping("/logout")
    public String redirectLogout() {
        return "redirect:/user/logout";
    }

    /**
     * Legacy URL: /registration (GET)
     * Redirect to /user/register for backward compatibility
     */
    @GetMapping("/registration")
    public String redirectRegistrationGet() {
        return "redirect:/user/register";
    }

    /**
     * Legacy URL: /registration (POST)
     * Forward to /user/register for backward compatibility
     */
    @PostMapping("/registration")
    public String redirectRegistrationPost() {
        return "forward:/user/register";
    }

    /**
     * Legacy URL: /check-username
     * Forward to /user/check-username for backward compatibility
     */
    @GetMapping("/check-username")
    public String redirectCheckUsername() {
        return "forward:/user/check-username";
    }

    /**
     * Legacy URL: /check-email
     * Forward to /user/check-email for backward compatibility
     */
    @GetMapping("/check-email")
    public String redirectCheckEmail() {
        return "forward:/user/check-email";
    }
}
