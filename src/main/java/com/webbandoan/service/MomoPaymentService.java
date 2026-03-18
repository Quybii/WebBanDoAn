package com.webbandoan.service;

import com.webbandoan.config.MomoProperties;
import com.webbandoan.dto.MomoCreatePaymentResponse;
import com.webbandoan.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MomoPaymentService {

    private final RestTemplate restTemplate;
    private final MomoProperties momoProperties;
    private final PaymentTransactionService paymentTransactionService;

    public MomoPaymentService(RestTemplate restTemplate, MomoProperties momoProperties, PaymentTransactionService paymentTransactionService) {
        this.restTemplate = restTemplate;
        this.momoProperties = momoProperties;
        this.paymentTransactionService = paymentTransactionService;
    }

    public MomoCreatePaymentResponse createPaymentRequest(Order order) {
        if (order == null || order.getId() == null || order.getTotalAmount() == null) {
            throw new IllegalArgumentException("Order không hợp lệ để tạo thanh toán MoMo");
        }

        String momoApiUrl = requireText(momoProperties.getMomoApiUrl(), "momo.momo-api-url");
        String partnerCode = requireText(momoProperties.getPartnerCode(), "momo.partner-code");
        String accessKey = requireText(momoProperties.getAccessKey(), "momo.access-key");
        String secretKey = requireText(momoProperties.getSecretKey(), "momo.secret-key");
        String requestType = requireText(momoProperties.getRequestType(), "momo.request-type");
        String returnUrl = requireText(momoProperties.getPayOrderReturnUrl(), "momo.pay-order.return-url");
        String notifyUrl = requireText(momoProperties.getPayOrderNotifyUrl(), "momo.pay-order.notify-url");
        String orderType = requireText(momoProperties.getOrderType(), "momo.order-type");
        boolean autoCapture = momoProperties.getAutoCapture() == null || momoProperties.getAutoCapture();
        String lang = momoProperties.getLang() != null && !momoProperties.getLang().isBlank() ? momoProperties.getLang() : "vi";

        String requestId = UUID.randomUUID().toString();
        String momoOrderId = buildMomoOrderId(order.getId(), requestId);
        Long amount = normalizeAmount(order.getTotalAmount());
        String orderInfo = buildOrderInfo(order);
        String extraData = buildExtraData(order.getId(), requestId);

        paymentTransactionService.createTransaction(order, requestId, orderInfo, order.getTotalAmount());

        String signature = buildSignature(secretKey, accessKey, partnerCode, requestType, returnUrl, notifyUrl, requestId, momoOrderId, amount.toString(), orderInfo, extraData);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", partnerCode);
        payload.put("accessKey", accessKey);
        payload.put("requestId", requestId);
        payload.put("amount", amount);
        payload.put("orderId", momoOrderId);
        payload.put("orderInfo", orderInfo);
        payload.put("orderType", orderType);
        payload.put("redirectUrl", returnUrl);
        payload.put("ipnUrl", notifyUrl);
        payload.put("extraData", extraData);
        payload.put("requestType", requestType);
        payload.put("autoCapture", autoCapture);
        payload.put("lang", lang);
        payload.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            log.info("Creating MoMo payment request for orderId={}, momoOrderId={}, amount={}, requestId={}", order.getId(), momoOrderId, amount, requestId);
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(payload, headers);
            MomoCreatePaymentResponse response = restTemplate.postForObject(
                    momoApiUrl,
                    httpEntity,
                    MomoCreatePaymentResponse.class
            );

            if (response == null) {
                throw new IllegalStateException("MoMo trả về response rỗng");
            }
            if (response.getRequestId() == null || response.getRequestId().isBlank()) {
                response.setRequestId(requestId);
            }
            if (response.getOrderId() == null || response.getOrderId().isBlank()) {
                response.setOrderId(momoOrderId);
            }
            return response;
        } catch (HttpStatusCodeException ex) {
            log.error("MoMo API trả về HTTP {} cho orderId={}, momoOrderId={}, body={}", ex.getStatusCode(), order.getId(), momoOrderId, ex.getResponseBodyAsString(), ex);
            throw new IllegalStateException("MoMo sandbox từ chối yêu cầu tạo link thanh toán: " + ex.getResponseBodyAsString(), ex);
        } catch (RestClientException ex) {
            log.error("Không tạo được yêu cầu thanh toán MoMo cho orderId={}, momoOrderId={}", order.getId(), momoOrderId, ex);
            throw new IllegalStateException("Không thể kết nối MoMo sandbox. Vui lòng thử lại.", ex);
        }
    }

    public String buildOrderInfo(Order order) {
        return "Thanh toan don hang #" + order.getId();
    }

    private Long normalizeAmount(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private String buildMomoOrderId(Long internalOrderId, String requestId) {
        String shortRequestId = requestId.replace("-", "");
        if (shortRequestId.length() > 12) {
            shortRequestId = shortRequestId.substring(0, 12);
        }
        return "OD" + internalOrderId + "-" + shortRequestId;
    }

    private String buildExtraData(Long internalOrderId, String requestId) {
        String json = "{\"orderId\":\"" + internalOrderId + "\",\"requestId\":\"" + requestId + "\"}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private String buildSignature(String secretKey, String accessKey, String partnerCode, String requestType, String returnUrl, String notifyUrl, String requestId, String orderId, String amount, String orderInfo, String extraData) {
        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + notifyUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + returnUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;
        return hmacSha256(secretKey, rawSignature);
    }

    private String requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Thiếu cấu hình MoMo: " + propertyName);
        }
        return value;
    }

    private String hmacSha256(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
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
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Không thể tạo chữ ký MoMo", ex);
        }
    }
}