package com.webbandoan.repository;

import com.webbandoan.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository: Food.
 * Dùng để truy vấn bảng foods (món ăn).
 */
@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    /**
     * Lấy các món đang bán (is_available = true), sắp theo id giảm dần.
     * Dùng cho "món nổi bật" trên trang chủ (lấy N món đầu).
     */
    List<Food> findTop8ByIsAvailableTrueOrderByIdDesc();

    /**
     * Phân trang: món đang bán, sắp theo id giảm dần.
     */
    Page<Food> findByIsAvailableTrueOrderByIdDesc(Pageable pageable);

    /**
     * Phân trang: lọc theo danh mục, món đang bán.
     */
    Page<Food> findByCategoryIdAndIsAvailableTrue(Long categoryId, Pageable pageable);

    /**
     * Tìm theo tên (chứa keyword), món đang bán, có phân trang.
     */
    Page<Food> findByNameContainingIgnoreCaseAndIsAvailableTrue(String keyword, Pageable pageable);

    // Similar items: same category, exclude current id, limit by id desc
    List<Food> findTop6ByCategoryIdAndIsAvailableTrueAndIdNotOrderByIdDesc(Long categoryId, Long idNot);

    // Hot items: order by number of orderDetails (most ordered). Use JPQL subquery to count.
    @org.springframework.data.jpa.repository.Query("SELECT f FROM Food f WHERE f.isAvailable = true ORDER BY (SELECT COUNT(od) FROM com.webbandoan.entity.OrderDetail od WHERE od.food = f) DESC")
    List<Food> findTopByPopularity(org.springframework.data.domain.Pageable pageable);
}
