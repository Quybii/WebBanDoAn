package com.webbandoan.service;

import com.webbandoan.entity.Order;
import com.webbandoan.entity.PaymentTransaction;
import com.webbandoan.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentTransactionService {
    private final PaymentTransactionRepository paymentTransactionRepository;

    public PaymentTransactionService(PaymentTransactionRepository paymentTransactionRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    /**
     * Tạo giao dịch thanh toán mới (dành cho Momo)
     */
    public PaymentTransaction createTransaction(Order order, String momoRequestId, String orderInfo, BigDecimal amount) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setMomoRequestId(momoRequestId);
        transaction.setOrderInfo(orderInfo);
        transaction.setAmount(amount);
        transaction.setPaymentStatus("PENDING");
        transaction.setCreatedDate(LocalDateTime.now());
        return paymentTransactionRepository.save(transaction);
    }

    /**
     * Cập nhật trạng thái giao dịch (khi nhận callback từ Momo)
     */
    public PaymentTransaction updateTransaction(Long transactionId, String momoTransactionId, String paymentStatus, Integer responseCode, String responseMessage) {
        if (transactionId == null) {
            return null;
        }
        Optional<PaymentTransaction> optional = paymentTransactionRepository.findById(transactionId);
        if (optional.isEmpty()) {
            return null;
        }
        
        PaymentTransaction transaction = optional.get();
        transaction.setMomoTransactionId(momoTransactionId);
        transaction.setPaymentStatus(paymentStatus);
        transaction.setResponseCode(responseCode);
        transaction.setResponseMessage(responseMessage);
        transaction.setUpdatedDate(LocalDateTime.now());
        return paymentTransactionRepository.save(transaction);
    }

    /**
     * Lấy danh sách giao dịch của một đơn hàng
     */
    public List<PaymentTransaction> getTransactionsByOrder(Long orderId) {
        return paymentTransactionRepository.findByOrderId(orderId);
    }

    /**
     * Lấy giao dịch mới nhất của một đơn hàng
     */
    public PaymentTransaction getLatestTransactionByOrder(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return paymentTransactionRepository.findTopByOrderIdOrderByCreatedDateDesc(orderId);
    }

    /**
     * Cập nhật giao dịch mới nhất của một đơn hàng.
     */
    public PaymentTransaction updateLatestTransactionByOrder(Long orderId, String momoTransactionId, String paymentStatus, Integer responseCode, String responseMessage) {
        PaymentTransaction transaction = getLatestTransactionByOrder(orderId);
        if (transaction == null) {
            return null;
        }
        return updateTransaction(transaction.getId(), momoTransactionId, paymentStatus, responseCode, responseMessage);
    }

    /**
     * Tìm giao dịch theo Momo transaction ID
     */
    public Optional<PaymentTransaction> findByMomoTransactionId(String momoTransactionId) {
        return paymentTransactionRepository.findByMomoTransactionId(momoTransactionId);
    }

    /**
     * Tìm giao dịch theo Momo request ID
     */
    public Optional<PaymentTransaction> findByMomoRequestId(String momoRequestId) {
        return paymentTransactionRepository.findByMomoRequestId(momoRequestId);
    }
}
