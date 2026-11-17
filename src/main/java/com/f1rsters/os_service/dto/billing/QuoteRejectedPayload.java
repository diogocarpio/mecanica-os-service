package com.f1rsters.os_service.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRejectedPayload {
    private String quoteId;
    private String status;
    private String reason;
    private Instant decidedAt;
}
