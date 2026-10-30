package com.f1rsters.os_service.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionHandlerTest {

    @Test
    void handler_ShouldExist() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        assertNotNull(handler);
    }
}
