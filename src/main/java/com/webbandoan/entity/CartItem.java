package com.webbandoan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity: CartItem (Một dòng trong giỏ hàng: user + món + số lượng)
 * Bảng: cart_items
 * Quan hệ: ManyToOne với User, ManyToOne với Food
 * Ràng buộc: Mỗi user chỉ có tối đa 1 dòng cho mỗi món (unique user_id + food_id)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "food_id"})
})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /** Constructor cho Service layer (tạo CartItem không có id) */
    public CartItem(User user, Food food, Integer quantity) {
        this.user = user;
        this.food = food;
        this.quantity = quantity;
    }

    /** Thành tiền = đơn giá * số lượng (dùng cho hiển thị). */
    public BigDecimal getSubtotal() {
        if (food == null || food.getPrice() == null || quantity == null) return BigDecimal.ZERO;
        return food.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
