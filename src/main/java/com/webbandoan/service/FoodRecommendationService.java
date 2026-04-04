package com.webbandoan.service;

import com.webbandoan.entity.Food;
import com.webbandoan.entity.FoodRecommendation;
import com.webbandoan.repository.FoodRecommendationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FoodRecommendationService {

    private final FoodRecommendationRepository foodRecommendationRepository;
    private final FoodService foodService;

    public FoodRecommendationService(FoodRecommendationRepository foodRecommendationRepository, FoodService foodService) {
        this.foodRecommendationRepository = foodRecommendationRepository;
        this.foodService = foodService;
    }

    @Transactional(readOnly = true)
    public List<FoodRecommendation> findAll() {
        return foodRecommendationRepository.findAllWithFoods();
    }

    @Transactional(readOnly = true)
    public Page<FoodRecommendation> findAllPaged(Pageable pageable) {
        return foodRecommendationRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public FoodRecommendation findById(Long id) {
        if (id == null) {
            return null;
        }
        return foodRecommendationRepository.findById(id).orElse(null);
    }

    @Transactional
    public FoodRecommendation save(Long id, Long foodId, Long recommendedFoodId, Integer priority) {
        if (foodId == null || recommendedFoodId == null) {
            return null;
        }
        if (foodId.equals(recommendedFoodId)) {
            return null;
        }

        Food food = foodService.findById(foodId);
        Food recommendedFood = foodService.findById(recommendedFoodId);
        if (food == null || recommendedFood == null) {
            return null;
        }

        FoodRecommendation recommendation = id != null
                ? foodRecommendationRepository.findById(id).orElse(new FoodRecommendation())
                : foodRecommendationRepository.findByFood_IdAndRecommendedFood_Id(foodId, recommendedFoodId).orElse(new FoodRecommendation());

        recommendation.setFood(food);
        recommendation.setRecommendedFood(recommendedFood);
        recommendation.setPriority(priority != null && priority > 0 ? priority : 1);
        return foodRecommendationRepository.save(recommendation);
    }

    @Transactional
    public void deleteById(Long id) {
        if (id != null) {
            foodRecommendationRepository.deleteById(id);
        }
    }
}
