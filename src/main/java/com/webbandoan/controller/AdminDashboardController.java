package com.webbandoan.controller;

import com.webbandoan.service.FoodService;
import com.webbandoan.service.OrderService;
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

    public AdminDashboardController(FoodService foodService, OrderService orderService) {
        this.foodService = foodService;
        this.orderService = orderService;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<com.webbandoan.entity.Food> foods = foodService.findAll();
        List<com.webbandoan.entity.Order> orders = orderService.findAll();
        long pendingOrders = orders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        model.addAttribute("totalFoods", foods.size());
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("pendingOrders", pendingOrders);
        return "admin/dashboard";
    }
}
