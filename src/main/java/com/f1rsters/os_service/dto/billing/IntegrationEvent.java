package com.f1rsters.os_service.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationEvent<T> {
    private UUID eventId;
    private String eventType;
    private int eventVersion;
    private Instant occurredAt;
    private String source;
    private String correlationId;
    private UUID causationId;
    private String osId;
    private T payload;
}
