package com.webbandoan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO: FoodRecommendationResponse
 * Dùng cho API endpoint /api/foods/{id}/recommendations
 * Chứa thông tin gợi ý kèm theo (món ăn được gợi ý)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodRecommendationResponse {

    private Long id;

    private String name;

    private BigDecimal price;

    private String imageUrl;

    private String description;
}
