package com.webbandoan.controller;

import com.webbandoan.entity.CartItem;
import com.webbandoan.entity.User;
import com.webbandoan.repository.UserRepository;
import com.webbandoan.service.CartService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller: Giỏ hàng.
 * - GET /cart : xem giỏ hàng.
 * - GET /cart/add/{foodId} : thêm món vào giỏ (redirect về /cart hoặc về trang món).
 * - POST /cart/update : cập nhật số lượng.
 * - POST /cart/remove/{id} : xóa dòng khỏi giỏ.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping
    public String viewCart(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        List<CartItem> items = cartService.getCartItems(user);
        BigDecimal total = cartService.getTotalAmount(user);
        model.addAttribute("cartItems", items);
        model.addAttribute("totalAmount", total);
        return "cart";
    }

    @GetMapping("/add/{foodId}")
    public String addToCart(
            @PathVariable Long foodId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) String redirectTo,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        cartService.addItem(user, foodId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm món vào giỏ.");
        if ("cart".equals(redirectTo)) {
            return "redirect:/cart";
        }
        return "redirect:/foods/" + foodId;
    }

    @PostMapping("/update")
    public String updateQuantity(
            @RequestParam Long cartItemId,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        cartService.updateQuantity(user, cartItemId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật giỏ hàng.");
        return "redirect:/cart";
    }

    @PostMapping("/remove/{id}")
    public String removeItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        if (cartService.removeItem(user, id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa món khỏi giỏ.");
        }
        return "redirect:/cart";
    }
}
