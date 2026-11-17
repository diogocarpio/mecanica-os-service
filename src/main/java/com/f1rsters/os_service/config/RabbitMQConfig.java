package com.f1rsters.os_service.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String OS_EXCHANGE = "os.exchange";
    public static final String MECANICA_EVENTS_EXCHANGE = "mecanica.events";

    public static final String OS_CREATED_QUEUE = "os.created.queue";
    public static final String OS_APPROVED_QUEUE = "os.approved.queue";
    public static final String OS_STATUS_UPDATED_QUEUE = "os.status.updated.queue";
    public static final String EXECUTION_COMPLETED_QUEUE = "execution.completed.queue";
    public static final String OS_CANCELLED_QUEUE = "os.cancelled.queue";

    public static final String BILLING_QUOTE_SUBMITTED_QUEUE = "os.billing.quote-submitted.queue";
    public static final String BILLING_QUOTE_APPROVED_QUEUE = "os.billing.quote-approved.queue";
    public static final String BILLING_QUOTE_REJECTED_QUEUE = "os.billing.quote-rejected.queue";
    public static final String BILLING_QUOTE_FAILED_QUEUE = "os.billing.quote-failed.queue";

    @Bean
    public MessageConverter converter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(OS_EXCHANGE);
    }

    @Bean
    public TopicExchange mecanicaEventsExchange() {
        return new TopicExchange(MECANICA_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue osCreatedQueue() {
        return new Queue(OS_CREATED_QUEUE, true);
    }

    @Bean
    public Queue osApprovedQueue() {
        return new Queue(OS_APPROVED_QUEUE, true);
    }

    @Bean
    public Queue osStatusUpdatedQueue() {
        return new Queue(OS_STATUS_UPDATED_QUEUE, true);
    }

    @Bean
    public Queue billingQuoteSubmittedQueue() {
        return new Queue(BILLING_QUOTE_SUBMITTED_QUEUE, true);
    }

    @Bean
    public Queue billingQuoteApprovedQueue() {
        return new Queue(BILLING_QUOTE_APPROVED_QUEUE, true);
    }

    @Bean
    public Queue billingQuoteRejectedQueue() {
        return new Queue(BILLING_QUOTE_REJECTED_QUEUE, true);
    }

    @Bean
    public Queue billingQuoteFailedQueue() {
        return new Queue(BILLING_QUOTE_FAILED_QUEUE, true);
    }

    @Bean
    public Queue executionCompletedQueue() {
        return new Queue(EXECUTION_COMPLETED_QUEUE, true);
    }

    @Bean
    public Queue osCancelledQueue() {
        return new Queue(OS_CANCELLED_QUEUE, true);
    }

    @Bean
    public Binding osCreatedBinding() {
        return BindingBuilder.bind(osCreatedQueue())
                .to(exchange())
                .with("os.created");
    }

    @Bean
    public Binding osApprovedBinding() {
        return BindingBuilder.bind(osApprovedQueue())
                .to(exchange())
                .with("os.approved");
    }

    @Bean
    public Binding osStatusUpdatedBinding() {
        return BindingBuilder.bind(osStatusUpdatedQueue())
                .to(exchange())
                .with("os.status.updated");
    }

    @Bean
    public Binding billingQuoteSubmittedBinding() {
        return BindingBuilder.bind(billingQuoteSubmittedQueue())
                .to(mecanicaEventsExchange())
                .with("billing.quote.submitted.v1");
    }

    @Bean
    public Binding billingQuoteApprovedBinding() {
        return BindingBuilder.bind(billingQuoteApprovedQueue())
                .to(mecanicaEventsExchange())
                .with("billing.quote.approved.v1");
    }

    @Bean
    public Binding billingQuoteRejectedBinding() {
        return BindingBuilder.bind(billingQuoteRejectedQueue())
                .to(mecanicaEventsExchange())
                .with("billing.quote.rejected.v1");
    }

    @Bean
    public Binding billingQuoteFailedBinding() {
        return BindingBuilder.bind(billingQuoteFailedQueue())
                .to(mecanicaEventsExchange())
                .with("billing.quote.failed.v1");
    }

    @Bean
    public Binding executionCompletedBinding() {
        return BindingBuilder.bind(executionCompletedQueue())
                .to(exchange())
                .with("execution.completed");
    }

    @Bean
    public Binding osCancelledBinding() {
        return BindingBuilder.bind(osCancelledQueue())
                .to(exchange())
                .with("os.cancelled");
    }
}
