package com.webbandoan.service;

import com.webbandoan.config.VnpayProperties;
import com.webbandoan.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Slf4j
@Service
public class VnpayPaymentService {

    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnpayProperties vnpayProperties;

    public VnpayPaymentService(VnpayProperties vnpayProperties) {
        this.vnpayProperties = vnpayProperties;
    }

    public String createPaymentUrl(Order order, String ipAddress) {
        if (order == null || order.getId() == null || order.getTotalAmount() == null) {
            throw new IllegalArgumentException("Order không hợp lệ để tạo thanh toán VNPay");
        }

        String payUrl = requireText(vnpayProperties.getPayUrl(), "vnpay.pay-url");
        String tmnCode = requireText(vnpayProperties.getTmnCode(), "vnpay.tmn-code");
        String hashSecret = requireText(vnpayProperties.getHashSecret(), "vnpay.hash-secret");
        String returnUrl = requireText(vnpayProperties.getReturnUrl(), "vnpay.return-url");
        String version = Optional.ofNullable(vnpayProperties.getVersion()).filter(value -> !value.isBlank()).orElse("2.1.0");
        String command = Optional.ofNullable(vnpayProperties.getCommand()).filter(value -> !value.isBlank()).orElse("pay");
        String currCode = Optional.ofNullable(vnpayProperties.getCurrCode()).filter(value -> !value.isBlank()).orElse("VND");
        String locale = Optional.ofNullable(vnpayProperties.getLocale()).filter(value -> !value.isBlank()).orElse("vn");
        String orderType = Optional.ofNullable(vnpayProperties.getOrderType()).filter(value -> !value.isBlank()).orElse("other");
        int expireMinutes = Optional.ofNullable(vnpayProperties.getExpireMinutes()).orElse(15);

        String txnRef = buildTxnRef(order.getId());
        String amount = normalizeAmount(order.getTotalAmount()).multiply(BigDecimal.valueOf(100L)).toPlainString();
        String createDate = LocalDateTime.now().format(VNPAY_DATE_FORMAT);
        String expireDate = LocalDateTime.now().plusMinutes(expireMinutes).format(VNPAY_DATE_FORMAT);
        String orderInfo = buildOrderInfo(order);

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Amount", amount);
        params.put("vnp_Command", command);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_CurrCode", currCode);
        params.put("vnp_ExpireDate", expireDate);
        params.put("vnp_IpAddr", ipAddress != null && !ipAddress.isBlank() ? ipAddress : "127.0.0.1");
        params.put("vnp_Locale", locale);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", orderType);
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_Version", version);

        String query = buildQueryString(params);
        String secureHash = hmacSha512(hashSecret, query);
        return payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public boolean verifyReturnParams(Map<String, String> allParams) {
        if (allParams == null || allParams.isEmpty()) {
            return false;
        }

        String secureHash = allParams.get("vnp_SecureHash");
        if (secureHash == null || secureHash.isBlank()) {
            return false;
        }

        Map<String, String> filtered = new TreeMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || value == null) {
                continue;
            }
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) {
                continue;
            }
            if (value.isBlank()) {
                continue;
            }
            filtered.put(key, value);
        }

        String query = buildQueryString(filtered);
        String expectedHash = hmacSha512(requireText(vnpayProperties.getHashSecret(), "vnpay.hash-secret"), query);
        return secureHash.equalsIgnoreCase(expectedHash);
    }

    public Long extractOrderId(String txnRef) {
        if (txnRef == null || txnRef.isBlank()) {
            return null;
        }

        if (txnRef.startsWith("OD")) {
            int dashIndex = txnRef.indexOf('-');
            if (dashIndex > 2) {
                String numericPart = txnRef.substring(2, dashIndex);
                if (numericPart.matches("\\d+")) {
                    return Long.parseLong(numericPart);
                }
            }
        }

        if (txnRef.matches("\\d+")) {
            return Long.parseLong(txnRef);
        }

        return null;
    }

    public String buildOrderInfo(Order order) {
        return "Thanh toan don hang #" + order.getId();
    }

    private String buildTxnRef(Long orderId) {
        return "OD" + orderId + "-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP);
    }

    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("%20", "+");
    }

    private String hmacSha512(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo chữ ký VNPay", ex);
        }
    }

    private String requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Thiếu cấu hình VNPay: " + propertyName);
        }
        return value;
    }
}