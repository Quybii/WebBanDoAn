package com.webbandoan.repository;

import com.webbandoan.entity.Food;
import com.webbandoan.entity.FoodImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodImageRepository extends JpaRepository<FoodImage, Long> {
    List<FoodImage> findByFoodOrderByIdAsc(Food food);
}
