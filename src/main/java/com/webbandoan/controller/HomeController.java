package com.webbandoan.controller;

import com.webbandoan.entity.Category;
import com.webbandoan.entity.Food;
import com.webbandoan.service.CategoryService;
import com.webbandoan.service.FoodService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller: Trang chủ.
 * - GET / : hiển thị danh mục + món nổi bật.
 */
@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final FoodService foodService;

    public HomeController(CategoryService categoryService, FoodService foodService) {
        this.categoryService = categoryService;
        this.foodService = foodService;
    }

    @GetMapping("/")
    public String home(CsrfToken csrfToken, Model model) {
        List<Category> categories = categoryService.findAll();
        List<Food> featuredFoods = foodService.findFeatured();
        model.addAttribute("categories", categories);
        model.addAttribute("featuredFoods", featuredFoods);
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "home";
    }
}
