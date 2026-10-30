package com.f1rsters.os_service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class OsServiceApplicationTest {

    @Test
    void application_ShouldExist() {
        assertNotNull(OsServiceApplication.class);
    }

    @Test
    void main_ShouldRunApplication() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            OsServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});
            mocked.verify(() -> SpringApplication.run(OsServiceApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }
}
