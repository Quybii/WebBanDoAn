package com.webbandoan.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Momo Create Payment Response Model
 * Phản hồi từ MoMo API khi tạo yêu cầu thanh toán
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoCreatePaymentResponse {
    private String requestId;

    @JsonAlias({"resultCode", "errorCode"})
    private Integer resultCode;

    private String orderId;
    private String message;
    private String localMessage;
    private String requestType;
    private String payUrl;       // URL để khách hàng thanh toán
    private String signature;
    private String qrCodeUrl;
    private String deeplink;
    private String deeplinkWebInApp;

    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }

    public Integer getErrorCode() {
        return resultCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.resultCode = errorCode;
    }
}
