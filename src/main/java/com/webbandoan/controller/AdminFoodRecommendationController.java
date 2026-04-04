package com.webbandoan.controller;

import com.webbandoan.entity.Food;
import com.webbandoan.entity.FoodRecommendation;
import com.webbandoan.service.CategoryService;
import com.webbandoan.service.FoodRecommendationService;
import com.webbandoan.service.FoodService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/food-recommendations")
public class AdminFoodRecommendationController {

    private final FoodRecommendationService foodRecommendationService;
    private final FoodService foodService;
    private final CategoryService categoryService;

    public AdminFoodRecommendationController(FoodRecommendationService foodRecommendationService, FoodService foodService, CategoryService categoryService) {
        this.foodRecommendationService = foodRecommendationService;
        this.foodService = foodService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.asc("food.id"),
                org.springframework.data.domain.Sort.Order.asc("priority"),
                org.springframework.data.domain.Sort.Order.asc("id")
        ));
        Page<FoodRecommendation> foodRecommendationPage = foodRecommendationService.findAllPaged(pageable);
        List<Food> foods = foodService.findAll();
        model.addAttribute("foodRecommendationPage", foodRecommendationPage);
        model.addAttribute("foodRecommendations", foodRecommendationPage.getContent());
        model.addAttribute("foods", foods);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", foodRecommendationPage.getTotalPages());
        model.addAttribute("totalElements", foodRecommendationPage.getTotalElements());
        return "admin/food-recommendations";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam(required = false) Long id,
            @RequestParam Long foodId,
            @RequestParam Long recommendedFoodId,
            @RequestParam(required = false) Integer priority,
            RedirectAttributes redirectAttributes) {
        FoodRecommendation saved = foodRecommendationService.save(id, foodId, recommendedFoodId, priority);
        if (saved == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể lưu gợi ý. Kiểm tra món chính, món kèm theo hoặc trùng dữ liệu.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", id == null ? "Đã thêm món kèm theo cho món chính." : "Đã cập nhật gợi ý.");
        }
        return "redirect:/admin/food-recommendations";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        foodRecommendationService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa gợi ý kèm theo.");
        return "redirect:/admin/food-recommendations";
    }
}