package com.webbandoan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity: Phương thức thanh toán
 * - COD: Thanh toán khi nhận hàng
 * - MOMO: Chuyển khoản Momo
 */
@Entity
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;  // COD, MOMO

    @Column(nullable = false, length = 200)
    private String name;  // Thanh toán khi nhận hàng, Chuyển khoản MoMo

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;
}
