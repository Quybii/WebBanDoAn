package com.webbandoan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vnpay")
public class VnpayProperties {
    private String payUrl;
    private String tmnCode;
    private String hashSecret;
    private String returnUrl;
    private String version;
    private String command;
    private String currCode;
    private String locale;
    private String orderType;
    private Integer expireMinutes;
}