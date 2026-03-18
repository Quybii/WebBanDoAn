package com.webbandoan.service;

import com.webbandoan.entity.PaymentMethod;
import com.webbandoan.repository.PaymentMethodRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepository;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    public List<PaymentMethod> getAllActive() {
        return paymentMethodRepository.findByIsActive(true);
    }

    public Optional<PaymentMethod> findByCode(String code) {
        return paymentMethodRepository.findByCode(code);
    }

    public PaymentMethod getById(Long id) {
        if (id == null) {
            return null;
        }
        return paymentMethodRepository.findById(id).orElse(null);
    }

    // Lấy phương thức COD (mặc định)
    public PaymentMethod getCOD() {
        return findByCode("COD").orElse(null);
    }

    // Lấy phương thức MOMO
    public PaymentMethod getMOMO() {
        return findByCode("MOMO").orElse(null);
    }
}
