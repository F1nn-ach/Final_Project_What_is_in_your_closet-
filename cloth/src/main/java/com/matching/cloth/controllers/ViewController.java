package com.matching.cloth.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.matching.cloth.models.Admin;
import com.matching.cloth.models.Astrologer;
import com.matching.cloth.models.Clothing;
import com.matching.cloth.models.ClothingType;
import com.matching.cloth.models.ColorTheory;
import com.matching.cloth.models.LuckyColor;
import com.matching.cloth.models.LuckyColorItem;
import com.matching.cloth.models.LuckyColorType;
import com.matching.cloth.models.LuckyListColor;
import com.matching.cloth.models.MainColorCategory;
import com.matching.cloth.models.SubColorCategory;
import com.matching.cloth.models.User;
import com.matching.cloth.service.AstrologerService;
import com.matching.cloth.service.ClothService;
import com.matching.cloth.service.ClothTypeService;
import com.matching.cloth.service.LuckyColorService;
import com.matching.cloth.service.LuckyColorTypeService; // Import @Value
import com.matching.cloth.service.LuckyListColorService;
import com.matching.cloth.service.MainColorCategoryService;
import com.matching.cloth.service.SubColorCategoryService;
import com.matching.cloth.services.ColorTheoryService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewController {

    @Autowired
    private LuckyColorService luckyColorService;

    @Autowired
    private AstrologerService astrologerService;

    @Autowired
    private LuckyColorTypeService luckyColorTypeService;

    @Autowired
    private LuckyListColorService luckyListColorService;

    @Autowired
    private ClothService clothService;

    @Autowired
    private MainColorCategoryService mainColorCategoryService;

    @Autowired
    private SubColorCategoryService subColorCategoryService;

    @Autowired
    private ClothTypeService clothTypeService;

    @Autowired
    private ColorTheoryService colorTheoryService;

    @Value("${image.base-url}")
    private String imageBaseUrl;

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

        if ("กาลกิณี".equals(luckyColor.getLuckyColorType().getLuckyColorTypeName())) {
            randomIndex = (randomIndex + 1) % luckyColors.size();
            luckyColor = luckyColors.get(randomIndex);
        }

        List<LuckyColorItem> items = luckyColor.getLuckyColorItems();
        LuckyColorItem selectedItem = items.get(random.nextInt(items.size()));

        modelAndView.addObject("selectedItem", selectedItem);
        return modelAndView;
    }

    @GetMapping("/user-login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/registration")
    public String getRegisterPage() {
        return "register";
    }

    @GetMapping("/list-item")
    public ModelAndView listClothes(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("list-clothes");

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                modelAndView.setViewName("redirect:/user-login");
                return modelAndView;
            }

            List<Clothing> clothes = clothService.findClothesByUser(user);
            List<ClothingType> clothTypes = clothTypeService.findAllClothTypes();

            List<SubColorCategory> userSubCategories = subColorCategoryService
                    .findUniqueSubCategoriesByClothes(clothes);
            List<MainColorCategory> userMainCategories = mainColorCategoryService
                    .findUniqueMainCategoriesByClothes(clothes);

            modelAndView.addObject("clothes", clothes);
            modelAndView.addObject("clothTypes", clothTypes);
            modelAndView.addObject("mainColorCategories", userMainCategories);
            modelAndView.addObject("subColorCategories", userSubCategories);
            modelAndView.addObject("imageBaseUrl", imageBaseUrl);

        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "เกิดข้อผิดพลาดในการโหลดรายการเสื้อผ้า");
        }

        return modelAndView;
    }

    @GetMapping("/add-item")
    public ModelAndView getAddClothPage(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("add-cloth");
        User user = (User) session.getAttribute("user");
        if (user == null) {
            modelAndView.setViewName("redirect:/user-login");
            return modelAndView;
        }

        List<ClothingType> clothTypes = clothTypeService.findAllClothTypes();
        modelAndView.addObject("clothTypes", clothTypes);
        modelAndView.addObject("username", user.getUsername());
        modelAndView.addObject("imageBaseUrl", imageBaseUrl);
        return modelAndView;
    }

    @GetMapping("/matching-item")
    public ModelAndView getMatchingPage(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("matching-cloth");
        User user = (User) session.getAttribute("user");
        if (user == null) {
            modelAndView.setViewName("redirect:/user-login");
            return modelAndView;
        }

        try {
            List<Clothing> clothes = clothService.findClothesByUser(user);

            List<ClothingType> clothTypes = clothTypeService.findAllClothTypes();

            List<MainColorCategory> mainColorCategories = mainColorCategoryService.findAllMainColorCategories();
            List<SubColorCategory> subColorCategories = subColorCategoryService.findAllSubColorCategories();

            List<Astrologer> astrologers = astrologerService.findAllAstrologer();

            List<LuckyColorType> luckyColorTypes = luckyColorTypeService.findAllLuckyColorTypes()
                    .stream()
                    .filter(type -> !"กาลกีนี".equals(type.getLuckyColorTypeName()))
                    .toList();

            String[] days = { "อาทิตย์", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์" };
            LocalDate today = LocalDate.now();
            String currentDay = days[today.getDayOfWeek().getValue() % 7];

            List<ColorTheory> colorTheories = colorTheoryService.getAllColorTheories();

            List<LuckyColor> allLuckyColors = luckyColorService.findAllLuckyColors();

            modelAndView.addObject("clothes", clothes);
            modelAndView.addObject("clothTypes", clothTypes);
            modelAndView.addObject("mainColorCategories", mainColorCategories);
            modelAndView.addObject("subColorCategories", subColorCategories);
            modelAndView.addObject("astrologers", astrologers);
            modelAndView.addObject("luckyColorTypes", luckyColorTypes);
            modelAndView.addObject("daysOfWeek", days);
            modelAndView.addObject("currentDay", currentDay);
            modelAndView.addObject("colorTheories", colorTheories);
            modelAndView.addObject("imageBaseUrl", imageBaseUrl);
            modelAndView.addObject("allLuckyColors", allLuckyColors);

        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "เกิดข้อผิดพลาดในการโหลดข้อมูล");
        }

        return modelAndView;
    }

    @GetMapping("/dashboard")
    public ModelAndView getAdminDashboard(HttpSession session, @RequestParam(value = "year", required = false) Integer year) {
        ModelAndView modelAndView = new ModelAndView("admin_dashboard");
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            modelAndView.setViewName("redirect:/user-login");
            return modelAndView;
        }

        int currentYear = (year != null) ? year : LocalDate.now().getYear();

        List<Astrologer> astrologers = astrologerService.findAllAstrologer();
        List<LuckyColor> allLuckyColors = luckyColorService.findAllLuckyColors();

        String[] days = { "อาทิตย์", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์" };
        List<LuckyColorType> luckyColorTypes = luckyColorTypeService.findAllLuckyColorTypes();
        List<LuckyListColor> luckyListColors = luckyListColorService.findAllLuckyListColor();

        modelAndView.addObject("currentYear", currentYear);
        modelAndView.addObject("astrologers", astrologers);
        modelAndView.addObject("luckyColors", allLuckyColors);
        modelAndView.addObject("daysOfWeek", days);
        modelAndView.addObject("luckyColorTypes", luckyColorTypes);
        modelAndView.addObject("luckyListColors", luckyListColors);

        return modelAndView;
    }


}
