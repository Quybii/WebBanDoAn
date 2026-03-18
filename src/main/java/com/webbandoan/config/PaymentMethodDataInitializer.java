package com.webbandoan.config;

import com.webbandoan.entity.PaymentMethod;
import com.webbandoan.repository.PaymentMethodRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentMethodDataInitializer {

    @Bean
    public CommandLineRunner initPaymentMethods(PaymentMethodRepository paymentMethodRepository) {
        return args -> {
            if (paymentMethodRepository.findByCode("COD").isEmpty()) {
                PaymentMethod cod = new PaymentMethod();
                cod.setCode("COD");
                cod.setName("Thanh toan khi nhan hang");
                cod.setDescription("Thanh toan tien cho nhan vien khi nhan duoc don hang");
                cod.setIsActive(true);
                paymentMethodRepository.save(cod);
            }

            if (paymentMethodRepository.findByCode("MOMO").isEmpty()) {
                PaymentMethod momo = new PaymentMethod();
                momo.setCode("MOMO");
                momo.setName("Chuyen khoan qua MoMo");
                momo.setDescription("Thanh toan qua vi MoMo hoac tai khoan ngan hang lien ket");
                momo.setIsActive(true);
                paymentMethodRepository.save(momo);
            }
        };
    }
}
