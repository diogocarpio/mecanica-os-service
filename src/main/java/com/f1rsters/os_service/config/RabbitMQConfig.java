package com.f1rsters.os_service.config;

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
    public static final String OS_CREATED_QUEUE = "os.created.queue";
    public static final String OS_APPROVED_QUEUE = "os.approved.queue";
    public static final String OS_STATUS_UPDATED_QUEUE = "os.status.updated.queue";

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
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
}
