package com.webbandoan.repository;

import com.webbandoan.entity.Food;
import com.webbandoan.entity.Review;
import com.webbandoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Lấy danh sách đánh giá của một món ăn, sắp xếp mới nhất lên đầu
    List<Review> findByFoodOrderByCreatedAtDesc(Food food);
    // Thêm hàm này để tìm đánh giá cũ của user cho món ăn này
    Optional<Review> findByUserAndFood(User user, Food food);
}