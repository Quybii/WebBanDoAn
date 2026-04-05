package com.webbandoan.controller;

import com.webbandoan.entity.Category;
import com.webbandoan.entity.FoodImage;
import com.webbandoan.entity.Food;
import com.webbandoan.entity.FoodReview;
import com.webbandoan.entity.User;
import com.webbandoan.service.CategoryService;
import com.webbandoan.service.FoodImageService;
import com.webbandoan.service.FoodReviewService;
import com.webbandoan.service.FoodService;
import com.webbandoan.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller: Trang danh sách món ăn và trang chi tiết món.
 * - GET /foods : danh sách món (phân trang, lọc, tìm kiếm).
 * - GET /foods/{id} : chi tiết một món, nút thêm vào giỏ.
 */
@Controller
public class FoodController {

    private final FoodService foodService;
    private final CategoryService categoryService;
    private final FoodImageService foodImageService;
    private final FoodReviewService foodReviewService;
    private final UserRepository userRepository;

    public FoodController(FoodService foodService, CategoryService categoryService, FoodImageService foodImageService,
                          FoodReviewService foodReviewService, UserRepository userRepository) {
        this.foodService = foodService;
        this.categoryService = categoryService;
        this.foodImageService = foodImageService;
        this.foodReviewService = foodReviewService;
        this.userRepository = userRepository;
    }

    @GetMapping("/foods")
    public String list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            CsrfToken csrfToken,
            Model model) {

        // Tạo Pageable: page (số trang, bắt đầu từ 0), size (số món mỗi trang)
        Pageable pageable = PageRequest.of(page, size);

        Page<Food> foodPage;

        // Logic: ưu tiên tìm kiếm keyword, sau đó lọc category, cuối cùng là tất cả
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Tìm kiếm theo tên
            foodPage = foodService.searchByName(keyword.trim(), pageable);
        } else if (categoryId != null) {
            // Lọc theo danh mục
            foodPage = foodService.findByCategoryId(categoryId, pageable);
        } else {
            // Tất cả món đang bán
            foodPage = foodService.findAllAvailable(pageable);
        }

        // Lấy danh sách danh mục để hiển thị dropdown lọc
        List<Category> categories = categoryService.findAll();

        // Đưa dữ liệu vào Model
        model.addAttribute("foodPage", foodPage);
        model.addAttribute("foods", foodPage.getContent()); // List<Food> của trang hiện tại
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("keyword", keyword != null ? keyword.trim() : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", foodPage.getTotalPages());
        model.addAttribute("totalElements", foodPage.getTotalElements());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }

        return "food-list";
    }

    /**
     * Trang chi tiết một món.
     * GET /foods/{id}
     */
    @GetMapping("/foods/{id}")
    public String detail(@PathVariable Long id, CsrfToken csrfToken, Model model) {
        Food food = foodService.findById(id);
        if (food == null) {
            return "redirect:/foods";
        }
        User currentUser = getCurrentUser();
        model.addAttribute("food", food);
        List<FoodImage> foodImages = foodImageService.findByFood(food);
        model.addAttribute("foodImages", foodImages);
        model.addAttribute("recommendedFoods", foodService.getRecommendations(food.getId(), 6));
        List<FoodReview> foodReviews = foodReviewService.findByFood(food.getId());
        model.addAttribute("foodReviews", foodReviews);
        model.addAttribute("reviewCount", foodReviewService.countByFood(food.getId()));
        model.addAttribute("averageRating", foodReviewService.getAverageRating(food.getId()));
        model.addAttribute("canReview", foodReviewService.canReview(currentUser, food.getId()));
        model.addAttribute("hasReviewed", foodReviewService.hasReviewed(currentUser, food.getId()));
        model.addAttribute("currentReview", foodReviewService.findReviewByUserAndFood(currentUser, food.getId()));
        model.addAttribute("currentUser", currentUser);
        // add categories/menu similar to home
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);

        // similar foods (same category)
        List<Food> similar = foodService.findSimilar(food.getCategory().getId(), food.getId(), 6);
        model.addAttribute("similarFoods", similar);

        // hot items (most ordered)
        List<Food> hot = foodService.findHot(6);
        hot.forEach(item -> item.setImageUrl(foodImageService.findPrimaryImageUrl(item)));
        model.addAttribute("hotFoods", hot);

        // on-sale / deals
        List<Food> deals = foodService.findOnSale(6);
        deals.forEach(item -> item.setImageUrl(foodImageService.findPrimaryImageUrl(item)));
        model.addAttribute("dealFoods", deals);
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "food-detail";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }
}
