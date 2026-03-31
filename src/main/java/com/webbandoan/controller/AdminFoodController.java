package com.webbandoan.controller;

import com.webbandoan.entity.Category;
import com.webbandoan.entity.Food;
import com.webbandoan.service.FoodImageService;
import com.webbandoan.service.CategoryService;
import com.webbandoan.service.FoodService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller Admin: CRUD món ăn.
 * - GET /admin/foods : danh sách món (admin).
 * - GET /admin/foods/new : form tạo món mới.
 * - GET /admin/foods/{id}/edit : form sửa món.
 * - POST /admin/foods : lưu món mới.
 * - POST /admin/foods/{id} : cập nhật món.
 * - POST /admin/foods/{id}/delete : xóa món.
 */
@Controller
@RequestMapping("/admin/foods")
public class AdminFoodController {

    private final FoodService foodService;
    private final CategoryService categoryService;
    private final FoodImageService foodImageService;

    public AdminFoodController(FoodService foodService, CategoryService categoryService, FoodImageService foodImageService) {
        this.foodService = foodService;
        this.categoryService = categoryService;
        this.foodImageService = foodImageService;
    }

    @GetMapping
    public String list(Model model) {
        List<Food> foods = foodService.findAll();
        List<Category> categories = categoryService.findAll();
        model.addAttribute("foods", foods);
        model.addAttribute("categories", categories);
        return "admin/food-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("food", new Food());
        model.addAttribute("categories", categories);
        return "admin/food-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Food food = foodService.findById(id);
        if (food == null) {
            return "redirect:/admin/foods";
        }
        List<Category> categories = categoryService.findAll();
        model.addAttribute("food", food);
        model.addAttribute("images", foodImageService.findByFood(food));
        model.addAttribute("categories", categories);
        return "admin/food-form";
    }

    @PostMapping({"", "/save"})
    public String save(
            @RequestParam(required = false) Long categoryId,
            @ModelAttribute Food food,
            @RequestParam(value = "images", required = false) org.springframework.web.multipart.MultipartFile[] images,
            RedirectAttributes redirectAttributes) {
        Food existingFood = food.getId() != null ? foodService.findById(food.getId()) : null;
        if (existingFood != null && food.getImageUrl() == null) {
            food.setImageUrl(existingFood.getImageUrl());
        }

        if (food.getName() == null || food.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên món không được để trống.");
            return food.getId() == null ? "redirect:/admin/foods/new" : "redirect:/admin/foods/" + food.getId() + "/edit";
        }
        if (food.getPrice() == null || food.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giá món phải >= 0.");
            return food.getId() == null ? "redirect:/admin/foods/new" : "redirect:/admin/foods/" + food.getId() + "/edit";
        }
        if (categoryId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn danh mục.");
            return food.getId() == null ? "redirect:/admin/foods/new" : "redirect:/admin/foods/" + food.getId() + "/edit";
        }
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Danh mục không tồn tại.");
            return food.getId() == null ? "redirect:/admin/foods/new" : "redirect:/admin/foods/" + food.getId() + "/edit";
        }
        food.setCategory(category);
        if (food.getIsAvailable() == null) {
            food.setIsAvailable(true);
        }
        Food savedFood = foodService.save(food);

        // store uploaded images if provided
        if (images != null && images.length > 0) {
            var savedImages = foodImageService.storeImages(savedFood, images);
            if (savedImages != null && !savedImages.isEmpty()) {
                savedFood.setImageUrl(savedImages.get(0).getImageUrl());
                foodService.save(savedFood);
            }
        }
        redirectAttributes.addFlashAttribute("successMessage", food.getId() == null ? "Đã thêm món mới." : "Đã cập nhật món.");
        return "redirect:/admin/foods";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        foodService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa món.");
        return "redirect:/admin/foods";
    }

    @PostMapping("/images/{imageId}/delete")
    public String deleteImage(@PathVariable Long imageId, RedirectAttributes redirectAttributes) {
        foodImageService.deleteImage(imageId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa hình.");
        return "redirect:/admin/foods";
    }

    @PostMapping("/images/{imageId}/set-main")
    public String setMainImage(@PathVariable Long imageId, RedirectAttributes redirectAttributes) {
        foodImageService.setMain(imageId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã đặt làm ảnh chính.");
        return "redirect:/admin/foods";
    }
}
