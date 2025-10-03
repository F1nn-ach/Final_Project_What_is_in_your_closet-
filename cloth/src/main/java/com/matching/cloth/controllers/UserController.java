package com.matching.cloth.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.matching.cloth.models.Admin;
import com.matching.cloth.models.User;
import com.matching.cloth.service.AdminService;
import com.matching.cloth.service.PasswordUtil;
import com.matching.cloth.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    private final String salt = "fynn$#2";

    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsername(@RequestParam String username) {
        if (userService.isUsernameExists(username) || adminService.isExistAdmin(username)) {
            return true;
        }

        return false;
    }

    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return userService.isEmailExists(email);
    }

    @PostMapping("/registration")
    public ModelAndView registerUser(@RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone) {

        ModelAndView modelAndView = new ModelAndView("register");

        User result;

        try {
            password = PasswordUtil.getInstance().createPassword(password, salt);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        result = userService.saveOrUpdateUser(user);

        if (result.getUsername().equals(username)) {
            System.out.println("Create user success!!");
            return new ModelAndView("redirect:/user-login");
        } else {
            System.out.println("Fail to create user!!");
            return modelAndView;
        }
    }

    @PostMapping("/login-check")
    public String loginCheck(HttpSession session,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(name = "rememberMe", required = false, defaultValue = "false") boolean rememberMe,
            org.springframework.ui.Model model) {

        Admin admin = adminService.findByUsername(username);
        if (admin != null) {
            if (admin.getAdminPassword().equals(password)) {
                session.setAttribute("admin", admin);
                session.setMaxInactiveInterval(15 * 60);
                session.setAttribute("userType", "admin");
                redirectAttributes.addFlashAttribute("successMessage", "เข้าสู่ระบบสำเร็จ");
                return "redirect:/dashboard";
            } else {
                model.addAttribute("error", "ชื่อผู้ใช้ หรือ รหัสผ่านไม่ถูกต้อง");
                return "login";
            }
        }

        Optional<User> userOpt = userService.findByUsername(username);
        User user = userOpt.orElse(null);

        if (user == null) {
            user = userService.findByEmail(username);
        }

        if (user != null) {
            try {
                String hashedPassword = PasswordUtil.getInstance().createPassword(password, salt);
                if (user.getPassword().equals(hashedPassword)) {
                    session.setAttribute("user", user);
                    session.setMaxInactiveInterval(15 * 60);
                    session.setAttribute("userType", "user");

                    String cookieValue = PasswordUtil.getInstance().createRememberMeCookie(username, password);
                    Cookie rememberMeCookie = new Cookie("rememberMe", cookieValue);
                    rememberMeCookie.setMaxAge(7 * 24 * 60 * 60);

                    if (rememberMe) {
                        response.addCookie(rememberMeCookie);
                    }

                    redirectAttributes.addFlashAttribute("successMessage",
                            "เข้าสู่ระบบสำเร็จ ยินดีต้อนรับคุณ " + user.getUsername());
                    return "redirect:/list-item";
                } else {
                    model.addAttribute("error", "ชื่อผู้ใช้ หรือ รหัสผ่านไม่ถูกต้อง");
                    return "login";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                model.addAttribute("error", "เกิดข้อผิดพลาด");
                return "login";
            }
        }

        model.addAttribute("error", "ชื่อผู้ใช้ หรือ รหัสผ่านไม่ถูกต้อง");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        session.invalidate();

        Cookie rememberMeCookie = new Cookie("rememberMe", null);
        rememberMeCookie.setMaxAge(0);
        response.addCookie(rememberMeCookie);

        redirectAttributes.addFlashAttribute("successMessage", "ออกจากระบบสำเร็จ");
        return "redirect:/";
    }

}
