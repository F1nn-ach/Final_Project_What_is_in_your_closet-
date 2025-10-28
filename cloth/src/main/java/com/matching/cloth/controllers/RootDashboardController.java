package com.matching.cloth.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling root-level /dashboard redirects
 * Provides backward compatibility for old dashboard URL
 */
@Controller
public class RootDashboardController {

    /**
     * Redirect /dashboard to /admin/dashboard
     * For backward compatibility with old admin dashboard URL
     */
    @GetMapping("/dashboard")
    public String redirectDashboard() {
        return "redirect:/admin/dashboard";
    }
}
