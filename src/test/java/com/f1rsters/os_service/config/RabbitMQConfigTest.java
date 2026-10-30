package com.f1rsters.os_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMQConfigTest {

    @Test
    void config_ShouldCreateBeans() {
        RabbitMQConfig config = new RabbitMQConfig();

        assertNotNull(config);
        assertNotNull(config.converter());
        assertInstanceOf(Jackson2JsonMessageConverter.class, config.converter());

        ConnectionFactory connectionFactory = org.mockito.Mockito.mock(ConnectionFactory.class);
        RabbitTemplate template = (RabbitTemplate) config.template(connectionFactory);
        assertNotNull(template);
        assertEquals(RabbitMQConfig.OS_EXCHANGE, config.exchange().getName());
        assertEquals(RabbitMQConfig.OS_CREATED_QUEUE, config.osCreatedQueue().getName());
        assertEquals(RabbitMQConfig.OS_APPROVED_QUEUE, config.osApprovedQueue().getName());
        assertEquals(RabbitMQConfig.OS_STATUS_UPDATED_QUEUE, config.osStatusUpdatedQueue().getName());
        assertEquals("os.created", config.osCreatedBinding().getRoutingKey());
        assertEquals("os.approved", config.osApprovedBinding().getRoutingKey());
        assertEquals("os.status.updated", config.osStatusUpdatedBinding().getRoutingKey());
    }
}
