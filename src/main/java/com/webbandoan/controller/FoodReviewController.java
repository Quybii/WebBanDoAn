package com.webbandoan.controller;

import com.webbandoan.entity.User;
import com.webbandoan.repository.UserRepository;
import com.webbandoan.service.FoodReviewService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/foods")
public class FoodReviewController {

    private final FoodReviewService foodReviewService;
    private final UserRepository userRepository;

    public FoodReviewController(FoodReviewService foodReviewService, UserRepository userRepository) {
        this.foodReviewService = foodReviewService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{foodId}/reviews")
    public String submitReview(
            @PathVariable Long foodId,
            @RequestParam(required = false) Long reviewId,
            @RequestParam int rating,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) MultipartFile[] images,
            @RequestParam(required = false) List<Long> removeImageIds,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!foodReviewService.canReview(currentUser, foodId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ khách hàng đã mua và đơn đã hoàn tất mới được đánh giá sản phẩm này.");
            return "redirect:/foods/" + foodId;
        }

        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số sao phải từ 1 đến 5.");
            return "redirect:/foods/" + foodId;
        }

        if (reviewId == null) {
            if (foodReviewService.hasReviewed(currentUser, foodId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã đánh giá sản phẩm này rồi.");
                return "redirect:/foods/" + foodId;
            }

            if (foodReviewService.createReview(currentUser, foodId, rating, comment, images) == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể lưu đánh giá, vui lòng thử lại.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Đã gửi đánh giá thành công.");
            }
        } else {
            if (foodReviewService.updateReview(currentUser, foodId, reviewId, rating, comment, images, removeImageIds) == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật đánh giá, vui lòng thử lại.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật đánh giá thành công.");
            }
        }

        return "redirect:/foods/" + foodId;
    }

    @PostMapping("/{foodId}/reviews/{reviewId}/delete")
    public String deleteReview(
            @PathVariable Long foodId,
            @PathVariable Long reviewId,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!foodReviewService.deleteReview(currentUser, foodId, reviewId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa đánh giá.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá.");
        }

        return "redirect:/foods/" + foodId;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }
}