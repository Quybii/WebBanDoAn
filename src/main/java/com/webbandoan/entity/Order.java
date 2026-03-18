package com.webbandoan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Order (Đơn hàng)
 * Bảng: orders
 * Quan hệ: ManyToOne với User, OneToMany với OrderDetail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // PENDING, CONFIRMED, DELIVERING, COMPLETED, CANCELLED

    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "note", length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;  // COD hoặc MOMO

    @Column(name = "payment_status", nullable = false, length = 50)
    private String paymentStatus = "PENDING";  // PENDING, COMPLETED, FAILED, CANCELLED

    @Column(name = "transaction_id", length = 100)
    private String transactionId;  // Mã giao dịch Momo (nếu thanh toán Momo)

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
        if (paymentStatus == null) {
            paymentStatus = "PENDING";
        }
    }
}
