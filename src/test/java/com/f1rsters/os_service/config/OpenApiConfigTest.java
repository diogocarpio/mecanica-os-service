package com.f1rsters.os_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void osServiceOpenAPI_ShouldReturnOpenAPI() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.osServiceOpenAPI();
        
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("OS Service API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }
}
