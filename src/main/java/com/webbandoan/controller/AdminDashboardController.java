package com.webbandoan.controller;

import com.webbandoan.dto.DashboardAnalyticsResponse;
import com.webbandoan.service.FoodService;
import com.webbandoan.service.AdminDashboardAnalyticsService;
import com.webbandoan.service.OrderService;
import com.webbandoan.service.ShopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.YearMonth;
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
    private final AdminDashboardAnalyticsService analyticsService;

    public AdminDashboardController(FoodService foodService, OrderService orderService, ShopService shopService,
                                    AdminDashboardAnalyticsService analyticsService) {
        this.foodService = foodService;
        this.orderService = orderService;
        this.shopService = shopService;
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) String month, Model model) {
        List<com.webbandoan.entity.Food> foods = foodService.findAll();
        List<com.webbandoan.entity.Order> orders = orderService.findAll();
        long pendingOrders = orders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        YearMonth selectedMonth = resolveMonth(month);

        model.addAttribute("totalFoods", foods.size());
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("shopOpen", shopService.isOpen());
        model.addAttribute("selectedMonth", selectedMonth.toString());
        return "admin/dashboard";
    }

    @GetMapping("/dashboard/chart-data")
    @ResponseBody
    public DashboardAnalyticsResponse chartData(@RequestParam(required = false) String month) {
        return analyticsService.getAnalytics(resolveMonth(month));
    }

    private YearMonth resolveMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month);
        } catch (Exception ex) {
            return YearMonth.now();
        }
    }

    @PostMapping("/shop/toggle")
    public String toggleShop(@RequestParam boolean open, RedirectAttributes redirectAttributes) {
        shopService.setOpen(open);
        redirectAttributes.addFlashAttribute("successMessage", open ? "Đã mở cửa nhận đơn." : "Đã tạm đóng cửa, không nhận đơn.");
        return "redirect:/admin";
    }
}
