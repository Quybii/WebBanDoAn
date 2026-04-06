package com.webbandoan.service;

import com.webbandoan.entity.Food;
import com.webbandoan.entity.FoodRecommendation;
import com.webbandoan.repository.FoodRepository;
import com.webbandoan.repository.FoodRecommendationRepository;
import com.webbandoan.repository.CartItemRepository;
import com.webbandoan.repository.FoodImageRepository;
import com.webbandoan.repository.FoodReviewRepository;
import com.webbandoan.repository.OrderDetailRepository;
import com.webbandoan.service.FoodReviewImageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service: Food.
 * Logic nghiệp vụ liên quan món ăn (trang chủ món nổi bật, danh sách, chi tiết, ...).
 */
@Service
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodRecommendationRepository recommendationRepository;
    private final FoodReviewRepository foodReviewRepository;
    private final FoodImageRepository foodImageRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final FoodReviewImageService foodReviewImageService;

    public FoodService(FoodRepository foodRepository,
                       FoodRecommendationRepository recommendationRepository,
                       FoodReviewRepository foodReviewRepository,
                       FoodImageRepository foodImageRepository,
                       CartItemRepository cartItemRepository,
                       OrderDetailRepository orderDetailRepository,
                       FoodReviewImageService foodReviewImageService) {
        this.foodRepository = foodRepository;
        this.recommendationRepository = recommendationRepository;
        this.foodReviewRepository = foodReviewRepository;
        this.foodImageRepository = foodImageRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.foodReviewImageService = foodReviewImageService;
    }

    /**
     * Lấy danh sách món nổi bật (tối đa 8 món đang bán, sắp mới nhất).
     * Dùng cho trang chủ.
     */
    @Transactional(readOnly = true)
    public List<Food> findFeatured() {
        return foodRepository.findTop8ByIsAvailableTrueAndIsAddonFalseOrderByIdDesc();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Food> findSimilar(Long categoryId, Long excludeId, int limit) {
        return foodRepository.findTop6ByCategoryIdAndIsAvailableTrueAndIsAddonFalseAndIdNotOrderByIdDesc(categoryId, excludeId);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Food> findHot(int limit) {
        return foodRepository.findTopByPopularity(org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Food> findOnSale(int limit) {
        // No discount field in model; reuse featured as 'on sale' placeholder
        return findFeatured();
    }

    /**
     * Phân trang: tất cả món đang bán.
     */
    @Transactional(readOnly = true)
    public Page<Food> findAllAvailable(Pageable pageable) {
        return foodRepository.findByIsAvailableTrueAndIsAddonFalseOrderByIdDesc(pageable);
    }

    /**
     * Phân trang: món theo danh mục.
     */
    @Transactional(readOnly = true)
    public Page<Food> findByCategoryId(Long categoryId, Pageable pageable) {
        return foodRepository.findByCategoryIdAndIsAvailableTrueAndIsAddonFalse(categoryId, pageable);
    }

    /**
     * Tìm theo tên (keyword), có phân trang.
     */
    @Transactional(readOnly = true)
    public Page<Food> searchByName(String keyword, Pageable pageable) {
        return foodRepository.findByNameContainingIgnoreCaseAndIsAvailableTrueAndIsAddonFalse(keyword, pageable);
    }

    /**
     * Lấy một món theo id (dùng cho trang chi tiết).
     */
    @Transactional(readOnly = true)
    public Food findById(Long id) {
        if (id == null) return null;
        return foodRepository.findById(id).orElse(null);
    }

    /**
     * Lấy tất cả món (admin dùng - không lọc is_available).
     */
    @Transactional(readOnly = true)
    public List<Food> findAll() {
        return foodRepository.findAllByOrderByIdDesc();
    }

    /**
     * Lấy danh sách món cho admin có phân trang.
     */
    @Transactional(readOnly = true)
    public Page<Food> findAllPaged(Pageable pageable) {
        return foodRepository.findAllByOrderByIdDesc(pageable);
    }

    /**
     * Lưu món (tạo mới hoặc cập nhật).
     */
    @Transactional
    public Food save(Food food) {
        if (food == null) return null;
        return foodRepository.save(food);
    }

    /**
     * Xóa món theo id.
     */
    @Transactional
    public void deleteById(Long id) {
        if (id != null) {
            cartItemRepository.deleteByFoodId(id);
            orderDetailRepository.deleteByFoodId(id);
            recommendationRepository.deleteAllByFoodId(id);
            Food food = foodRepository.findById(id).orElse(null);
            if (food != null) {
                foodReviewRepository.findByFoodOrderByCreatedAtDesc(food)
                        .forEach(foodReviewImageService::deleteByReview);
                foodReviewRepository.findByFoodOrderByCreatedAtDesc(food)
                        .forEach(foodReviewRepository::delete);
                foodImageRepository.findByFoodOrderByIdAsc(food)
                        .forEach(foodImageRepository::delete);
                foodRepository.delete(food);
            }
        }
    }

    /**
     * Lấy danh sách gợi ý kèm theo cho một món.
     * Ví dụ: Khi chọn "Bún bò" thì gợi ý "Bún thêm", "Thịt thêm", etc.
     * 
     * @param foodId ID của món chính
     * @return Danh sách gợi ý (đã eager load Food objects)
     */
    @Transactional(readOnly = true)
    public List<Food> getRecommendations(Long foodId) {
        if (foodId == null) return List.of();
        
        List<FoodRecommendation> recommendations = recommendationRepository.findRecommendationsByFoodId(foodId);
        return recommendations.stream()
                .map(FoodRecommendation::getRecommendedFood)
            .filter(f -> f != null && Boolean.TRUE.equals(f.getIsAvailable()))
                .collect(Collectors.toList());
    }

    /**
     * Lấy tối đa N gợi ý kèm theo cho một món.
     * 
     * @param foodId ID của món chính
     * @param limit Số lượng tối đa
     * @return Danh sách gợi ý, tối đa N phần tử
     */
    @Transactional(readOnly = true)
    public List<Food> getRecommendations(Long foodId, int limit) {
        if (foodId == null || limit <= 0) return List.of();
        
        List<FoodRecommendation> recommendations = recommendationRepository.findRecommendationsByFoodId(foodId);
        return recommendations.stream()
                .limit(limit)
                .map(FoodRecommendation::getRecommendedFood)
            .filter(f -> f != null && Boolean.TRUE.equals(f.getIsAvailable()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    

}
