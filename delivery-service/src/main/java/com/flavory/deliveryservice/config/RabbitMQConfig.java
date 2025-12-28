package com.flavory.deliveryservice.config;

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

    public static final String ORDER_EXCHANGE = "order.events";
    public static final String DELIVERY_EXCHANGE = "delivery.events";
    public static final String DLX_EXCHANGE = "dlx.exchange";

    public static final String ORDER_READY_QUEUE = "delivery.order.ready.queue";

    public static final String DELIVERY_STARTED_QUEUE = "order.delivery.started.queue";
    public static final String DELIVERY_PICKED_UP_QUEUE = "order.delivery.picked_up.queue";
    public static final String DELIVERY_COMPLETED_QUEUE = "order.delivery.completed.queue";

    public static final String ORDER_READY_ROUTING_KEY = "order.ready";
    public static final String DELIVERY_STARTED_ROUTING_KEY = "delivery.started";
    public static final String DELIVERY_PICKED_UP_ROUTING_KEY = "delivery.picked_up";
    public static final String DELIVERY_COMPLETED_ROUTING_KEY = "delivery.completed";

    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder
                .topicExchange(ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange deliveryExchange() {
        return ExchangeBuilder
                .topicExchange(DELIVERY_EXCHANGE)
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
    public Queue orderReadyQueue() {
        return QueueBuilder
                .durable(ORDER_READY_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.order.ready")
                .build();
    }

    @Bean
    public Binding orderReadyBinding(Queue orderReadyQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(orderReadyQueue)
                .to(orderExchange)
                .with(ORDER_READY_ROUTING_KEY);
    }

    @Bean
    public Queue deliveryStartedQueue() {
        return QueueBuilder
                .durable(DELIVERY_STARTED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.delivery.started")
                .build();
    }

    @Bean
    public Binding deliveryStartedBinding(Queue deliveryStartedQueue, TopicExchange deliveryExchange) {
        return BindingBuilder
                .bind(deliveryStartedQueue)
                .to(deliveryExchange)
                .with(DELIVERY_STARTED_ROUTING_KEY);
    }

    @Bean
    public Queue deliveryPickedUpQueue() {
        return QueueBuilder
                .durable(DELIVERY_PICKED_UP_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.delivery.picked_up")
                .build();
    }

    @Bean
    public Binding deliveryPickedUpBinding(Queue deliveryPickedUpQueue, TopicExchange deliveryExchange) {
        return BindingBuilder
                .bind(deliveryPickedUpQueue)
                .to(deliveryExchange)
                .with(DELIVERY_PICKED_UP_ROUTING_KEY);
    }

    @Bean
    public Queue deliveryCompletedQueue() {
        return QueueBuilder
                .durable(DELIVERY_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.delivery.completed")
                .build();
    }

    @Bean
    public Binding deliveryCompletedBinding(Queue deliveryCompletedQueue, TopicExchange deliveryExchange) {
        return BindingBuilder
                .bind(deliveryCompletedQueue)
                .to(deliveryExchange)
                .with(DELIVERY_COMPLETED_ROUTING_KEY);
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