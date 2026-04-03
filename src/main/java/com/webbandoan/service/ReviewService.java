package com.webbandoan.service;

import com.webbandoan.entity.Food;
import com.webbandoan.entity.Review;
import com.webbandoan.entity.User;
import com.webbandoan.repository.OrderRepository;
import com.webbandoan.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository, OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
    }

    // Lấy tất cả đánh giá của 1 món
    @Transactional(readOnly = true)
    public List<Review> findByFood(Food food) {
        return reviewRepository.findByFoodOrderByCreatedAtDesc(food);
    }

    // Kiểm tra xem User đã đủ điều kiện đánh giá chưa
    @Transactional(readOnly = true)
    public boolean canUserReview(User user, Food food) {
        if (user == null || food == null) return false;
        return orderRepository.hasUserPurchasedFood(user, food);
    }

    // Lưu đánh giá mới
    @Transactional
    public Review saveReview(User user, Food food, Integer rating, String comment) {
        if (!canUserReview(user, food)) {
            throw new IllegalArgumentException("Bạn chỉ có thể đánh giá món ăn đã mua thành công!");
        }

        // Tìm xem user đã đánh giá món này chưa
        Review review = reviewRepository.findByUserAndFood(user, food)
                                        .orElse(new Review()); // Nếu chưa có thì tạo object mới

        review.setUser(user);
        review.setFood(food);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now()); 

        return reviewRepository.save(review);
    }
    
    public Review getUserReviewForFood(User user, Food food) {
        return reviewRepository.findByUserAndFood(user, food).orElse(null);
    }
}   