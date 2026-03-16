package com.webbandoan.service;

import com.webbandoan.entity.Food;
import com.webbandoan.repository.FoodRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service: Food.
 * Logic nghiệp vụ liên quan món ăn (trang chủ món nổi bật, danh sách, chi tiết, ...).
 */
@Service
public class FoodService {

    private final FoodRepository foodRepository;

    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    /**
     * Lấy danh sách món nổi bật (tối đa 8 món đang bán, sắp mới nhất).
     * Dùng cho trang chủ.
     */
    @Transactional(readOnly = true)
    public List<Food> findFeatured() {
        return foodRepository.findTop8ByIsAvailableTrueOrderByIdDesc();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Food> findSimilar(Long categoryId, Long excludeId, int limit) {
        return foodRepository.findTop6ByCategoryIdAndIsAvailableTrueAndIdNotOrderByIdDesc(categoryId, excludeId);
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
        return foodRepository.findByIsAvailableTrueOrderByIdDesc(pageable);
    }

    /**
     * Phân trang: món theo danh mục.
     */
    @Transactional(readOnly = true)
    public Page<Food> findByCategoryId(Long categoryId, Pageable pageable) {
        return foodRepository.findByCategoryIdAndIsAvailableTrue(categoryId, pageable);
    }

    /**
     * Tìm theo tên (keyword), có phân trang.
     */
    @Transactional(readOnly = true)
    public Page<Food> searchByName(String keyword, Pageable pageable) {
        return foodRepository.findByNameContainingIgnoreCaseAndIsAvailableTrue(keyword, pageable);
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
        return foodRepository.findAll();
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
            foodRepository.deleteById(id);
        }
    }
}
