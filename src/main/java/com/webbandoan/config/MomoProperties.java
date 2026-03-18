package com.webbandoan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "momo")
public class MomoProperties {
    private String momoApiUrl;
    private String partnerCode;
    private String accessKey;
    private String secretKey;
    private String requestType;
    private String orderType;
    private Boolean autoCapture;
    private String lang;
    private String payOrderReturnUrl;
    private String payOrderNotifyUrl;
}