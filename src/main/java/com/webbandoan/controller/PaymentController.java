package com.webbandoan.controller;

import com.webbandoan.entity.Order;
import com.webbandoan.service.OrderService;
import com.webbandoan.service.PaymentTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final OrderService orderService;
    private final PaymentTransactionService paymentTransactionService;

    public PaymentController(OrderService orderService, PaymentTransactionService paymentTransactionService) {
        this.orderService = orderService;
        this.paymentTransactionService = paymentTransactionService;
    }

    @GetMapping("/momo-return")
    public String momoReturn(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String extraData,
            @RequestParam(required = false) String transId,
            @RequestParam(required = false) Integer resultCode,
            @RequestParam(required = false) String message,
            RedirectAttributes redirectAttributes) {

        Long internalOrderId = resolveInternalOrderId(orderId, requestId, extraData);
        if (internalOrderId == null) {
            return "redirect:/";
        }

        updatePaymentState(internalOrderId, transId, resultCode, message);
        if (resultCode != null && resultCode == 0) {
            redirectAttributes.addAttribute("orderId", internalOrderId);
            redirectAttributes.addFlashAttribute("successMessage", "Thanh toán MoMo đã được ghi nhận.");
            return "redirect:/order-success";
        }

        orderService.cancelMomoOrderAndRestoreCart(internalOrderId);

        String failureMessage = "Thanh toán MoMo không thành công.";
        if (message != null && !message.isBlank()) {
            failureMessage = message;
        }
        redirectAttributes.addFlashAttribute("errorMessage", failureMessage);
        return "redirect:/checkout";
    }

    @PostMapping(value = "/momo-callback", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE})
    public ResponseEntity<Map<String, Object>> momoCallback(@RequestBody(required = false) Map<String, Object> payload) {
        Map<String, Object> requestPayload = payload != null ? payload : new LinkedHashMap<>();

        String orderId = toStringValue(requestPayload.get("orderId"));
        String requestId = toStringValue(requestPayload.get("requestId"));
        String extraData = toStringValue(requestPayload.get("extraData"));
        String transId = toStringValue(requestPayload.get("transId"));
        Integer resultCode = toInteger(requestPayload.getOrDefault("resultCode", requestPayload.get("errorCode")));
        String message = toStringValue(requestPayload.get("message"));

        Long internalOrderId = resolveInternalOrderId(orderId, requestId, extraData);

        if (internalOrderId != null) {
            updatePaymentState(internalOrderId, transId, resultCode, message);
            if (resultCode != null && resultCode != 0) {
                orderService.cancelMomoOrderAndRestoreCart(internalOrderId);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "OK");
        return ResponseEntity.ok(response);
    }

    private void updatePaymentState(Long orderId, String transId, Integer resultCode, String message) {
        Order order = orderService.findById(orderId);
        if (order == null) {
            log.warn("Không tìm thấy orderId={} khi cập nhật thanh toán MoMo", orderId);
            return;
        }

        String paymentStatus = (resultCode != null && resultCode == 0) ? "COMPLETED" : "FAILED";

        orderService.updatePaymentResult(orderId, transId, paymentStatus);
        paymentTransactionService.updateLatestTransactionByOrder(
                orderId,
                transId,
                (resultCode != null && resultCode == 0) ? "SUCCESS" : "FAILED",
                resultCode,
                message
        );
    }

    private Long resolveInternalOrderId(String orderId, String requestId, String extraData) {
        Long resolvedOrderId = extractOrderIdFromExtraData(extraData);
        if (resolvedOrderId != null) {
            return resolvedOrderId;
        }

        if (requestId != null && !requestId.isBlank()) {
            Optional<com.webbandoan.entity.PaymentTransaction> transaction = paymentTransactionService.findByMomoRequestId(requestId);
            if (transaction.isPresent() && transaction.get().getOrder() != null) {
                return transaction.get().getOrder().getId();
            }
        }

        if (orderId != null && orderId.matches("\\d+")) {
            return Long.parseLong(orderId);
        }

        if (orderId != null && orderId.startsWith("OD")) {
            int dashIndex = orderId.indexOf('-');
            if (dashIndex > 2) {
                String numericPart = orderId.substring(2, dashIndex);
                if (numericPart.matches("\\d+")) {
                    return Long.parseLong(numericPart);
                }
            }
        }

        return null;
    }

    private Long extractOrderIdFromExtraData(String extraData) {
        if (extraData == null || extraData.isBlank()) {
            return null;
        }

        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(extraData);
            String json = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
            int keyIndex = json.indexOf("\"orderId\"");
            if (keyIndex < 0) {
                return null;
            }
            int colonIndex = json.indexOf(':', keyIndex);
            if (colonIndex < 0) {
                return null;
            }
            int startQuote = json.indexOf('"', colonIndex);
            int endQuote = json.indexOf('"', startQuote + 1);
            if (startQuote < 0 || endQuote < 0 || endQuote <= startQuote + 1) {
                return null;
            }
            String numericValue = json.substring(startQuote + 1, endQuote);
            if (numericValue.matches("\\d+")) {
                return Long.parseLong(numericValue);
            }
        } catch (IllegalArgumentException ex) {
            log.warn("Không giải mã được extraData MoMo", ex);
        }

        return null;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
