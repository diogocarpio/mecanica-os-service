package com.f1rsters.os_service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilityConstructorsTest {

    @Test
    void constructors_ShouldBeCallable() {
        assertDoesNotThrow(() -> new InputNormalizer());
        assertDoesNotThrow(() -> new CpfCnpjValidator());
        assertDoesNotThrow(() -> new PlacaValidator());
        assertDoesNotThrow(() -> new SensitiveDataMasker());
    }
}
