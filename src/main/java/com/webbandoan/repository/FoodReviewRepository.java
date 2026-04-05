package com.webbandoan.repository;

import com.webbandoan.entity.Food;
import com.webbandoan.entity.FoodReview;
import com.webbandoan.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodReviewRepository extends JpaRepository<FoodReview, Long> {

    @EntityGraph(attributePaths = {"user", "images"})
    List<FoodReview> findByFoodOrderByCreatedAtDesc(Food food);

    @EntityGraph(attributePaths = {"user", "images"})
    Optional<FoodReview> findByFoodAndUser(Food food, User user);

    long countByFood(Food food);

    @Query("SELECT COALESCE(AVG(fr.rating), 0) FROM FoodReview fr WHERE fr.food = :food")
    Double getAverageRatingByFood(@Param("food") Food food);
}