package com.webbandoan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "food_review_images")
public class FoodReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private FoodReview review;

    @Column(name = "image_url", length = 1000, nullable = false)
    private String imageUrl;

    public FoodReviewImage(FoodReview review, String imageUrl) {
        this.review = review;
        this.imageUrl = imageUrl;
    }
}