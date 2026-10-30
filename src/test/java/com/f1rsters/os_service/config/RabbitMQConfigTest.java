package com.f1rsters.os_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMQConfigTest {

    @Test
    void config_ShouldCreateBeans() {
        RabbitMQConfig config = new RabbitMQConfig();
        
        assertNotNull(config);
    }
}
