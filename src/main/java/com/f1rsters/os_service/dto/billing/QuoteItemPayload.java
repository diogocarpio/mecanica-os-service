package com.f1rsters.os_service.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteItemPayload {
    private String description;
    private String type;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
}
