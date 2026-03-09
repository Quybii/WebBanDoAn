package com.webbandoan.controller;

import com.webbandoan.entity.CartItem;
import com.webbandoan.entity.Order;
import com.webbandoan.entity.User;
import com.webbandoan.repository.UserRepository;
import com.webbandoan.service.CartService;
import com.webbandoan.service.OrderService;
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
 * Controller: Đặt hàng (checkout), trang thành công, và lịch sử đơn hàng.
 * - GET /checkout : form đặt hàng (địa chỉ, SĐT, ghi chú) + tóm tắt giỏ.
 * - POST /checkout : xử lý đặt hàng, redirect /order-success?orderId=...
 * - GET /order-success : trang cảm ơn sau khi đặt hàng.
 * - GET /orders : danh sách đơn hàng của user (lịch sử).
 * - GET /orders/{id} : chi tiết một đơn hàng.
 */
@Controller
@RequestMapping
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, CartService cartService, UserRepository userRepository) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        List<CartItem> cartItems = cartService.getCartItems(user);
        BigDecimal totalAmount = cartService.getTotalAmount(user);

        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("user", user);
        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(
            @RequestParam String shippingAddress,
            @RequestParam String phone,
            @RequestParam(required = false) String note,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập địa chỉ giao hàng.");
            return "redirect:/checkout";
        }
        if (phone == null || phone.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập số điện thoại.");
            return "redirect:/checkout";
        }

        Order order = orderService.placeOrder(user, shippingAddress.trim(), phone.trim(), note);
        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng trống. Không thể đặt hàng.");
            return "redirect:/cart";
        }

        redirectAttributes.addAttribute("orderId", order.getId());
        return "redirect:/order-success";
    }

    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam Long orderId, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Order order = orderService.findByIdAndUser(orderId, user);
        if (order == null) {
            return "redirect:/";
        }
        model.addAttribute("order", order);
        return "order-success";
    }

    @GetMapping("/orders")
    public String orderHistory(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        List<Order> orders = orderService.findByUser(user);
        model.addAttribute("orders", orders);
        return "order-history";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Order order = orderService.findByIdAndUser(id, user);
        if (order == null) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        return "order-detail";
    }
}
