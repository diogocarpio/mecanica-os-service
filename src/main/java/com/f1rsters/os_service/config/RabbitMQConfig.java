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
    public static final String ORCAMENTO_APROVADO_QUEUE = "billing.orcamento.aprovado.queue";
    public static final String ORCAMENTO_REJEITADO_QUEUE = "billing.orcamento.rejeitado.queue";
    public static final String EXECUTION_COMPLETED_QUEUE = "execution.completed.queue";
    public static final String OS_CANCELLED_QUEUE = "os.cancelled.queue";

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
    public Queue orcamentoAprovadoQueue() {
        return new Queue(ORCAMENTO_APROVADO_QUEUE, true);
    }

    @Bean
    public Queue orcamentoRejeitadoQueue() {
        return new Queue(ORCAMENTO_REJEITADO_QUEUE, true);
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
    public Binding orcamentoAprovadoBinding() {
        return BindingBuilder.bind(orcamentoAprovadoQueue())
                .to(exchange())
                .with("orcamento.aprovado");
    }

    @Bean
    public Binding orcamentoRejeitadoBinding() {
        return BindingBuilder.bind(orcamentoRejeitadoQueue())
                .to(exchange())
                .with("orcamento.rejeitado");
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
