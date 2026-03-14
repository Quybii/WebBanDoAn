package com.webbandoan.controller;

import com.webbandoan.entity.User;
import com.webbandoan.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller: Đăng nhập (trang) và Đăng ký.
 * - GET /login : hiển thị form đăng nhập (POST /login do Spring Security xử lý).
 * - GET /register : hiển thị form đăng ký.
 * - POST /register : xử lý đăng ký (mã hóa mật khẩu, lưu user với role USER).
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Sai tên đăng nhập hoặc mật khẩu.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes) {

        username = username != null ? username.trim() : "";
        if (username.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập không được để trống.");
            return "redirect:/register";
        }
        if (password == null || password.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu không được để trống.");
            return "redirect:/register";
        }
        if (password.length() < 4) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu phải có ít nhất 4 ký tự.");
            return "redirect:/register";
        }
        if (userService.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập đã tồn tại.");
            return "redirect:/register";
        }

        User user = userService.register(username, password, fullName, email, phone, address);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đăng ký thất bại. Vui lòng thử lại.");
            return "redirect:/register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công. Vui lòng đăng nhập.");
        return "redirect:/login";
    }
}
