package com.webbandoan.repository;

import com.webbandoan.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
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
     * Lấy toàn bộ món cùng danh mục để hiển thị ở admin.
     */
    @EntityGraph(attributePaths = "category")
    List<Food> findAllByOrderByIdDesc();

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

        @Query(value = """
                        SELECT TOP 5 f.id, f.name, COALESCE(SUM(od.quantity), 0) AS total_sold
                        FROM order_details od
                        INNER JOIN orders o ON o.id = od.order_id
                        INNER JOIN foods f ON f.id = od.food_id
                        WHERE o.payment_status = 'COMPLETED'
                            AND o.order_date >= :startDate
                            AND o.order_date < :endDate
                        GROUP BY f.id, f.name
                        ORDER BY total_sold DESC
                        """, nativeQuery = true)
        List<Object[]> findTop5BestSellingFoodsBetween(java.time.LocalDateTime startDate,
                                                                                                     java.time.LocalDateTime endDate);
}
