package com.webbandoan.repository;

import com.webbandoan.entity.FoodReview;
import com.webbandoan.entity.FoodReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodReviewImageRepository extends JpaRepository<FoodReviewImage, Long> {
    List<FoodReviewImage> findByReviewOrderByIdAsc(FoodReview review);
}