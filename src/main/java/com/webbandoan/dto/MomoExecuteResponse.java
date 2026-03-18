package com.webbandoan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Momo Execute/Callback Response Model
 * Phản hồi từ MoMo khi khách hàng thanh toán xong (callback)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoExecuteResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private String amount;
    private String orderInfo;
    private String orderType;
    private String transId;        // Transaction ID từ MoMo
    private Integer errorCode;      // 0 = thành công
    private String message;
    private String payType;
    private LocalDateTime responseTime;
    private String extraData;
    private String signature;

    public boolean isSuccess() {
        return errorCode != null && errorCode == 0;
    }
}
