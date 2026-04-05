package com.webbandoan.service;

import com.webbandoan.entity.Food;
import com.webbandoan.entity.FoodReview;
import com.webbandoan.entity.Order;
import com.webbandoan.entity.User;
import com.webbandoan.repository.FoodReviewRepository;
import com.webbandoan.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class FoodReviewService {

    private final FoodReviewRepository foodReviewRepository;
    private final OrderRepository orderRepository;
    private final FoodService foodService;
    private final FoodReviewImageService foodReviewImageService;

    public FoodReviewService(FoodReviewRepository foodReviewRepository,
                             OrderRepository orderRepository,
                             FoodService foodService,
                             FoodReviewImageService foodReviewImageService) {
        this.foodReviewRepository = foodReviewRepository;
        this.orderRepository = orderRepository;
        this.foodService = foodService;
        this.foodReviewImageService = foodReviewImageService;
    }

    @Transactional(readOnly = true)
    public boolean canReview(User user, Long foodId) {
        if (user == null || foodId == null) {
            return false;
        }
        return !orderRepository.findCompletedOrdersContainingFood(user, foodId).isEmpty();
    }

    @Transactional(readOnly = true)
    public boolean hasReviewed(User user, Long foodId) {
        if (user == null || foodId == null) {
            return false;
        }
        Food food = foodService.findById(foodId);
        if (food == null) {
            return false;
        }
        return foodReviewRepository.findByFoodAndUser(food, user).isPresent();
    }

    @Transactional(readOnly = true)
    public FoodReview findReviewByUserAndFood(User user, Long foodId) {
        if (user == null || foodId == null) {
            return null;
        }
        Food food = foodService.findById(foodId);
        if (food == null) {
            return null;
        }
        return foodReviewRepository.findByFoodAndUser(food, user).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<FoodReview> findByFood(Long foodId) {
        Food food = foodService.findById(foodId);
        if (food == null) {
            return List.of();
        }
        return foodReviewRepository.findByFoodOrderByCreatedAtDesc(food);
    }

    @Transactional(readOnly = true)
    public long countByFood(Long foodId) {
        Food food = foodService.findById(foodId);
        if (food == null) {
            return 0L;
        }
        return foodReviewRepository.countByFood(food);
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Long foodId) {
        Food food = foodService.findById(foodId);
        if (food == null) {
            return 0D;
        }
        Double average = foodReviewRepository.getAverageRatingByFood(food);
        return average != null ? average : 0D;
    }

    @Transactional
    public FoodReview createReview(User user, Long foodId, int rating, String comment, MultipartFile[] images) {
        if (user == null || foodId == null) {
            return null;
        }
        Food food = foodService.findById(foodId);
        if (food == null || !canReview(user, foodId)) {
            return null;
        }
        if (rating < 1 || rating > 5) {
            return null;
        }
        if (foodReviewRepository.findByFoodAndUser(food, user).isPresent()) {
            return null;
        }

        List<Order> completedOrders = orderRepository.findCompletedOrdersContainingFood(user, foodId);
        if (completedOrders.isEmpty()) {
            return null;
        }

        FoodReview review = new FoodReview();
        review.setUser(user);
        review.setFood(food);
        review.setOrder(completedOrders.get(0));
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);

        FoodReview saved = foodReviewRepository.save(review);
        foodReviewImageService.storeImages(saved, images);
        return saved;
    }

    @Transactional
    public FoodReview updateReview(User user,
                                   Long foodId,
                                   Long reviewId,
                                   int rating,
                                   String comment,
                                   MultipartFile[] newImages,
                                   List<Long> removeImageIds) {
        if (user == null || foodId == null || reviewId == null) {
            return null;
        }

        FoodReview review = foodReviewRepository.findById(reviewId).orElse(null);
        Food food = foodService.findById(foodId);
        if (review == null || food == null) {
            return null;
        }
        if (review.getUser() == null || !review.getUser().getId().equals(user.getId())) {
            return null;
        }
        if (review.getFood() == null || !review.getFood().getId().equals(foodId)) {
            return null;
        }
        if (!canReview(user, foodId)) {
            return null;
        }
        if (rating < 1 || rating > 5) {
            return null;
        }

        foodReviewImageService.deleteImagesByIds(normalizeImageIds(removeImageIds));

        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);
        FoodReview saved = foodReviewRepository.save(review);
        foodReviewImageService.storeImages(saved, newImages);
        return saved;
    }

    @Transactional
    public boolean deleteReview(User user, Long foodId, Long reviewId) {
        if (user == null || foodId == null || reviewId == null) {
            return false;
        }

        FoodReview review = foodReviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return false;
        }
        if (review.getUser() == null || !review.getUser().getId().equals(user.getId())) {
            return false;
        }
        if (review.getFood() == null || !review.getFood().getId().equals(foodId)) {
            return false;
        }

        foodReviewImageService.deleteByReview(review);
        foodReviewRepository.delete(review);
        return true;
    }

    private List<Long> normalizeImageIds(List<Long> imageIds) {
        if (imageIds == null) {
            return List.of();
        }
        List<Long> cleaned = new ArrayList<>();
        for (Long id : imageIds) {
            if (id != null) {
                cleaned.add(id);
            }
        }
        return cleaned;
    }
}