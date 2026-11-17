package com.f1rsters.os_service.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusChangedPayload {
    private String paymentId;
    private String quoteId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String externalPaymentId;
    private String providerStatus;
    private Instant changedAt;
}
