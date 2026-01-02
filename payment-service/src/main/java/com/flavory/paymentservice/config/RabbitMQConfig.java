package com.flavory.paymentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMQConfig {

    public static final String PAYMENT_EXCHANGE = "payment.events";
    public static final String ORDER_EXCHANGE = "order.events";
    public static final String DLX_EXCHANGE = "dlx.exchange";

    public static final String PAYMENT_SUCCEEDED_QUEUE = "order.payment.succeeded.queue";
    public static final String PAYMENT_FAILED_QUEUE = "order.payment.failed.queue";
    public static final String PAYMENT_REFUNDED_QUEUE = "order.payment.refunded.queue";

    public static final String PAYMENT_SUCCEEDED_ROUTING_KEY = "payment.succeeded";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";
    public static final String PAYMENT_REFUNDED_ROUTING_KEY = "payment.refunded";

    @Bean
    public TopicExchange paymentExchange() {
        return ExchangeBuilder
                .topicExchange(PAYMENT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder
                .topicExchange(ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue paymentSucceededQueue() {
        return QueueBuilder
                .durable(PAYMENT_SUCCEEDED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.payment.succeeded")
                .build();
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder
                .durable(PAYMENT_FAILED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.payment.failed")
                .build();
    }

    @Bean
    public Queue paymentRefundedQueue() {
        return QueueBuilder
                .durable(PAYMENT_REFUNDED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.payment.refunded")
                .build();
    }

    @Bean
    public Binding paymentSucceededBinding() {
        return BindingBuilder
                .bind(paymentSucceededQueue())
                .to(paymentExchange())
                .with(PAYMENT_SUCCEEDED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder
                .bind(paymentFailedQueue())
                .to(paymentExchange())
                .with(PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentRefundedBinding() {
        return BindingBuilder
                .bind(paymentRefundedQueue())
                .to(paymentExchange())
                .with(PAYMENT_REFUNDED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());

        template.setRetryTemplate(new RetryTemplate());

        return template;
    }
}