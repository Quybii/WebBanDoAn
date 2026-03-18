package com.webbandoan.controller;

import com.webbandoan.entity.User;
import com.webbandoan.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String profilePage(Authentication auth, CsrfToken csrfToken, Model model) {
        if (auth == null) return "redirect:/login";
        String username = auth.getName();
        Optional<User> u = userRepository.findByUsername(username);
        if (u.isEmpty()) return "redirect:/login";
        model.addAttribute("user", u.get());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "profile";
    }

    @PostMapping
    public String updateProfile(Authentication auth,
                                @RequestParam(required = false) String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String addressLabel,
                                @RequestParam(required = false) Double latitude,
                                @RequestParam(required = false) Double longitude
    ) {
        if (auth == null) return "redirect:/login";
        String username = auth.getName();
        Optional<User> ou = userRepository.findByUsername(username);
        if (ou.isEmpty()) return "redirect:/login";
        User user = ou.get();
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        user.setAddressLabel(addressLabel);
        user.setLatitude(latitude);
        user.setLongitude(longitude);
        userRepository.save(user);
        return "redirect:/profile?success";
    }
}
