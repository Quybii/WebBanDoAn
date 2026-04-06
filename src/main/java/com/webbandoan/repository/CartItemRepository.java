package com.webbandoan.repository;

import com.webbandoan.entity.CartItem;
import com.webbandoan.entity.Food;
import com.webbandoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository: CartItem (giỏ hàng).
 * Dùng để truy vấn bảng cart_items.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserOrderByCreatedAtDesc(User user);

    java.util.Optional<CartItem> findByUserAndFoodAndParentCartItem(User user, Food food, CartItem parentCartItem);

    List<CartItem> findByUserAndParentCartItemOrderByCreatedAtDesc(User user, CartItem parentCartItem);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.parentCartItem = :parentCartItem")
    void deleteByParentCartItem(@Param("parentCartItem") CartItem parentCartItem);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user = :user")
    void deleteByUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.food.id = :foodId")
    void deleteByFoodId(@Param("foodId") Long foodId);
}
