package com.f1rsters.os_service.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusOrdemServicoTest {

    @Test
    void values_ShouldContainAllStatuses() {
        StatusOrdemServico[] statuses = StatusOrdemServico.values();
        
        assertEquals(6, statuses.length);
    }
}
