package com.f1rsters.os_service.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteApprovedPayload {
    private String quoteId;
    private BigDecimal total;
    private String currency;
    private String status;
    private Instant decidedAt;
}
