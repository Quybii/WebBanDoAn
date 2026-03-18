package com.webbandoan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity: Giao dịch thanh toán (dùng cho Momo)
 * Lưu thông tin chi tiết mỗi lần thanh toán
 */
@Entity
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "momo_transaction_id", length = 100)
    private String momoTransactionId;  // ID giao dịch từ Momo

    @Column(name = "momo_request_id", length = 100)
    private String momoRequestId;  // Request ID gửi tới Momo

    @Column(name = "order_info", length = 500)
    private String orderInfo;  // Mô tả giao dịch

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;  // Số tiền giao dịch

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;  // PENDING, SUCCESS, FAILED, CANCELLED

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "response_code")
    private Integer responseCode;  // Response code từ Momo API

    @Column(name = "response_message", length = 500)
    private String responseMessage;  // Response message từ Momo API

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (paymentStatus == null) {
            paymentStatus = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
