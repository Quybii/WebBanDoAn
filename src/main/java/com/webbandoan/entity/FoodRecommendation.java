package com.webbandoan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity: FoodRecommendation (Gợi ý kèm theo)
 * Bảng: food_recommendations
 * Quan hệ: ManyToOne với Food (food_id), ManyToOne với Food (recommended_food_id)
 * 
 * Ví dụ: Khi chọn "Bún bò" thì gợi ý "Bún thêm", "Thịt thêm", etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "food_recommendations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"food_id", "recommended_food_id"})
})
public class FoodRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Món chính (ví dụ: Bún bò)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    /**
     * Món được gợi ý (ví dụ: Bún thêm, Thịt thêm)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_food_id", nullable = false)
    private Food recommendedFood;

    /**
     * Thứ tự ưu tiên hiển thị (1 = cao nhất)
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 1;

}
