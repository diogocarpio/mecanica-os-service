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
        assertEquals(RabbitMQConfig.MECANICA_EVENTS_EXCHANGE, config.mecanicaEventsExchange().getName());
        assertEquals(RabbitMQConfig.BILLING_QUOTE_SUBMITTED_QUEUE, config.billingQuoteSubmittedQueue().getName());
        assertEquals(RabbitMQConfig.BILLING_QUOTE_APPROVED_QUEUE, config.billingQuoteApprovedQueue().getName());
        assertEquals(RabbitMQConfig.BILLING_QUOTE_REJECTED_QUEUE, config.billingQuoteRejectedQueue().getName());
        assertEquals(RabbitMQConfig.BILLING_QUOTE_FAILED_QUEUE, config.billingQuoteFailedQueue().getName());
        assertEquals(RabbitMQConfig.EXECUTION_COMPLETED_QUEUE, config.executionCompletedQueue().getName());
        assertEquals(RabbitMQConfig.OS_CANCELLED_QUEUE, config.osCancelledQueue().getName());
        assertEquals("os.created", config.osCreatedBinding().getRoutingKey());
        assertEquals("os.approved", config.osApprovedBinding().getRoutingKey());
        assertEquals("os.status.updated", config.osStatusUpdatedBinding().getRoutingKey());
        assertEquals("billing.quote.submitted.v1", config.billingQuoteSubmittedBinding().getRoutingKey());
        assertEquals("billing.quote.approved.v1", config.billingQuoteApprovedBinding().getRoutingKey());
        assertEquals("billing.quote.rejected.v1", config.billingQuoteRejectedBinding().getRoutingKey());
        assertEquals("billing.quote.failed.v1", config.billingQuoteFailedBinding().getRoutingKey());
        assertEquals("billing.payment.pending.v1", config.billingPaymentPendingBinding().getRoutingKey());
        assertEquals("billing.payment.approved.v1", config.billingPaymentApprovedBinding().getRoutingKey());
        assertEquals("billing.payment.rejected.v1", config.billingPaymentRejectedBinding().getRoutingKey());
        assertEquals("billing.payment.failed.v1", config.billingPaymentFailedBinding().getRoutingKey());
        assertEquals("execution.completed", config.executionCompletedBinding().getRoutingKey());
        assertEquals("os.cancelled", config.osCancelledBinding().getRoutingKey());
    }
}
