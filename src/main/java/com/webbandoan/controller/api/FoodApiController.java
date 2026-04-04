package com.webbandoan.controller.api;

import com.webbandoan.dto.FoodRecommendationResponse;
import com.webbandoan.entity.Food;
import com.webbandoan.service.FoodImageService;
import com.webbandoan.service.FoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API Controller: Food API
 * Cung cấp các endpoint API để client lấy dữ liệu:
 * - GET /api/foods/{id}/recommendations : Lấy danh sách gợi ý kèm theo
 */
@RestController
@RequestMapping("/api/foods")
public class FoodApiController {

    private final FoodService foodService;
    private final FoodImageService foodImageService;

    public FoodApiController(FoodService foodService, FoodImageService foodImageService) {
        this.foodService = foodService;
        this.foodImageService = foodImageService;
    }

    /**
     * API: Lấy danh sách gợi ý kèm theo cho một món
     * 
     * GET /api/foods/{id}/recommendations
     * Query params: limit (mặc định: 5)
     * 
     * Response:
     * [
     *   { id: 1, name: "Bún thêm", price: 15000, imageUrl: "...", description: "..." },
     *   { id: 2, name: "Thịt thêm", price: 20000, imageUrl: "...", description: "..." }
     * ]
     * 
     * @param foodId ID của món chính
     * @param limit Số lượng gợi ý tối đa (mặc định: 5)
     * @return Danh sách gợi ý
     */
    @GetMapping("/{id}/recommendations")
    public ResponseEntity<List<FoodRecommendationResponse>> getRecommendations(
            @PathVariable("id") Long foodId,
            @RequestParam(defaultValue = "5") int limit) {
        
        // Kiểm tra xem món chính có tồn tại không
        Food mainFood = foodService.findById(foodId);
        if (mainFood == null) {
            return ResponseEntity.notFound().build();
        }

        // Lấy danh sách gợi ý
        List<Food> recommendations = foodService.getRecommendations(foodId, Math.max(limit, 1));

        // Chuyển đổi sang DTO
        List<FoodRecommendationResponse> responses = recommendations.stream()
                .map(food -> new FoodRecommendationResponse(
                        food.getId(),
                        food.getName(),
                        food.getPrice(),
                foodImageService.findPrimaryImageUrl(food),
                        food.getDescription()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

}
