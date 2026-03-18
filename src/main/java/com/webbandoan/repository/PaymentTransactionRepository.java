package com.webbandoan.repository;

import com.webbandoan.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByOrderId(Long orderId);
    PaymentTransaction findTopByOrderIdOrderByCreatedDateDesc(Long orderId);
    Optional<PaymentTransaction> findByMomoRequestId(String momoRequestId);
    Optional<PaymentTransaction> findByMomoTransactionId(String momoTransactionId);
    List<PaymentTransaction> findByPaymentStatus(String paymentStatus);
}
