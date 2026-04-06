package com.webbandoan.repository;

import com.webbandoan.entity.FoodRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: FoodRecommendation.
 * Dùng để truy vấn bảng food_recommendations (gợi ý kèm theo).
 */
@Repository
public interface FoodRecommendationRepository extends JpaRepository<FoodRecommendation, Long> {

    /**
     * Lấy danh sách gợi ý cho một món (sắp theo priority, sau đó theo id)
     * Ví dụ: Khi chọn "Bún bò", lấy tất cả gợi ý liên quan
     * 
     * @param foodId ID của món chính
     * @return Danh sách gợi ý, sắp theo ưu tiên
     */
    @Query("SELECT r FROM FoodRecommendation r " +
           "JOIN FETCH r.recommendedFood " +
           "WHERE r.food.id = :foodId " +
           "ORDER BY r.priority ASC, r.id ASC")
    List<FoodRecommendation> findRecommendationsByFoodId(Long foodId);

        @Query("SELECT r FROM FoodRecommendation r " +
            "JOIN FETCH r.food " +
            "JOIN FETCH r.recommendedFood " +
            "ORDER BY r.food.id ASC, r.priority ASC, r.id ASC")
        List<FoodRecommendation> findAllWithFoods();

        @EntityGraph(attributePaths = {"food", "recommendedFood"})
        Page<FoodRecommendation> findAll(Pageable pageable);

        Optional<FoodRecommendation> findByFood_IdAndRecommendedFood_Id(Long foodId, Long recommendedFoodId);

        boolean existsByFood_IdAndRecommendedFood_Id(Long foodId, Long recommendedFoodId);

        @Modifying
        @Query("DELETE FROM FoodRecommendation r WHERE r.food.id = :foodId OR r.recommendedFood.id = :foodId")
        void deleteAllByFoodId(@Param("foodId") Long foodId);



}
