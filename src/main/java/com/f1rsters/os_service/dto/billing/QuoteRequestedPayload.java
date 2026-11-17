package com.f1rsters.os_service.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequestedPayload {
    private String customerId;
    private List<QuoteItemPayload> items;
    private String currency;
    private Instant validUntil;
}
