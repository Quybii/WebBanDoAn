package com.webbandoan.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "food_images")
public class FoodImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "image_url", length = 1000, nullable = false)
    private String imageUrl;

    @Column(name = "is_main", nullable = false)
    private Boolean isMain = false;

    public FoodImage() {
    }

    public FoodImage(Food food, String imageUrl, Boolean isMain) {
        this.food = food;
        this.imageUrl = imageUrl;
        this.isMain = isMain;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsMain() {
        return isMain;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }
}
