package com.webbandoan.controller;

import com.webbandoan.service.FoodService;
import com.webbandoan.service.OrderService;
import com.webbandoan.service.ShopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller Admin: Dashboard chính.
 * - GET /admin : trang dashboard với thống kê tổng quan.
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final FoodService foodService;
    private final OrderService orderService;
    private final ShopService shopService;

    public AdminDashboardController(FoodService foodService, OrderService orderService, ShopService shopService) {
        this.foodService = foodService;
        this.orderService = orderService;
        this.shopService = shopService;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<com.webbandoan.entity.Food> foods = foodService.findAll();
        List<com.webbandoan.entity.Order> orders = orderService.findAll();
        long pendingOrders = orders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        model.addAttribute("totalFoods", foods.size());
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("shopOpen", shopService.isOpen());
        return "admin/dashboard";
    }

    @org.springframework.web.bind.annotation.PostMapping("/shop/toggle")
    public String toggleShop(@org.springframework.web.bind.annotation.RequestParam boolean open, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        shopService.setOpen(open);
        redirectAttributes.addFlashAttribute("successMessage", open ? "Đã mở cửa nhận đơn." : "Đã tạm đóng cửa, không nhận đơn.");
        return "redirect:/admin";
    }
}
