package com.webbandoan.controller;

import com.webbandoan.entity.Category;
import com.webbandoan.entity.Food;
import com.webbandoan.entity.FoodImage;
import com.webbandoan.entity.Review;
import com.webbandoan.entity.User;
import com.webbandoan.service.CategoryService;
import com.webbandoan.service.FoodImageService;
import com.webbandoan.service.FoodService;
import com.webbandoan.service.ReviewService;
import com.webbandoan.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Controller: Trang danh sách món ăn và trang chi tiết món.
 * - GET /foods : danh sách món (phân trang, lọc, tìm kiếm).
 * - GET /foods/{id} : chi tiết một món, nút thêm vào giỏ.
 * - POST /foods/{id}/review : xử lý gửi đánh giá.
 */
@Controller
public class FoodController {

    private final FoodService foodService;
    private final CategoryService categoryService;
    private final FoodImageService foodImageService;
    private final ReviewService reviewService;
    private final UserService userService;

    public FoodController(FoodService foodService, CategoryService categoryService, 
                          FoodImageService foodImageService, ReviewService reviewService, 
                          UserService userService) {
        this.foodService = foodService;
        this.categoryService = categoryService;
        this.foodImageService = foodImageService;
        this.reviewService = reviewService;
        this.userService = userService;
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
    public String detail(@PathVariable Long id, Principal principal, CsrfToken csrfToken, Model model) {
        Food food = foodService.findById(id);
        if (food == null) {
            return "redirect:/foods";
        }
        model.addAttribute("food", food);
        
        // Hình ảnh phụ
        List<FoodImage> foodImages = foodImageService.findByFood(food);
        model.addAttribute("foodImages", foodImages);

        // Danh mục và gợi ý
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);

        List<Food> similar = foodService.findSimilar(food.getCategory().getId(), food.getId(), 6);
        model.addAttribute("similarFoods", similar);

        List<Food> hot = foodService.findHot(6);
        model.addAttribute("hotFoods", hot);

        List<Food> deals = foodService.findOnSale(6);
        model.addAttribute("dealFoods", deals);
        
        // Xử lý Review (Đánh giá)
        List<Review> reviews = reviewService.findByFood(food);
        model.addAttribute("reviews", reviews);

        boolean canReview = false;
        Review userReview = null;
        if (principal != null) {
            User currentUser = userService.findByUsername(principal.getName());
            canReview = reviewService.canUserReview(currentUser, food);
            userReview = reviewService.getUserReviewForFood(currentUser, food);       
        }
        model.addAttribute("canReview", canReview);
        model.addAttribute("userReview", userReview);

        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "food-detail";
    }

    /**
     * API xử lý submit form đánh giá
     * POST /foods/{id}/review
     */
    @PostMapping("/foods/{id}/review")
    public String submitReview(@PathVariable Long id, 
                               @RequestParam Integer rating, 
                               @RequestParam String comment, 
                               Principal principal, 
                               RedirectAttributes redirectAttrs) {
        // Chưa đăng nhập thì đẩy về trang login
        if (principal == null) {
            return "redirect:/login"; 
        }
        
        try {
            User user = userService.findByUsername(principal.getName());
            Food food = foodService.findById(id);
            if (food == null) {
                return "redirect:/foods";
            }
            
            reviewService.saveReview(user, food, rating, comment);
            redirectAttrs.addFlashAttribute("successMsg", "Cảm ơn bạn đã gửi đánh giá!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        
        // Redirect lại chính trang chi tiết món ăn đó
        return "redirect:/foods/" + id;
    }
    
}