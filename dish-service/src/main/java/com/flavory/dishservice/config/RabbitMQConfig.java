package com.flavory.dishservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String DISH_EXCHANGE = "dish.events";
    public static final String ORDER_EXCHANGE = "order.events";
    public static final String USER_EXCHANGE = "user.events";

    public static final String DISH_CREATED_QUEUE = "dish.created.queue";
    public static final String DISH_UPDATED_QUEUE = "dish.updated.queue";
    public static final String DISH_DELETED_QUEUE = "dish.deleted.queue";
    public static final String DISH_AVAILABILITY_CHANGED_QUEUE = "dish.availability.changed.queue";

    public static final String DISH_CREATED_ROUTING_KEY = "dish.created";
    public static final String DISH_UPDATED_ROUTING_KEY = "dish.updated";
    public static final String DISH_DELETED_ROUTING_KEY = "dish.deleted";
    public static final String DISH_AVAILABILITY_CHANGED_ROUTING_KEY = "dish.availability.changed";

    public static final String DLX_EXCHANGE = "dlx.exchange";

    @Bean
    public TopicExchange dishExchange() {
        return ExchangeBuilder
                .topicExchange(DISH_EXCHANGE)
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
    public TopicExchange userExchange() {
        return ExchangeBuilder
                .topicExchange(USER_EXCHANGE)
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
    public Queue dishCreatedQueue() {
        return QueueBuilder
                .durable(DISH_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.dish.created")
                .build();
    }

    @Bean
    public Queue dishUpdatedQueue() {
        return QueueBuilder
                .durable(DISH_UPDATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.dish.updated")
                .build();
    }

    @Bean
    public Queue dishDeletedQueue() {
        return QueueBuilder
                .durable(DISH_DELETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.dish.deleted")
                .build();
    }

    @Bean
    public Queue dishAvailabilityChangedQueue() {
        return QueueBuilder
                .durable(DISH_AVAILABILITY_CHANGED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.dish.availability.changed")
                .build();
    }

    @Bean
    public Binding dishCreatedBinding(Queue dishCreatedQueue, TopicExchange dishExchange) {
        return BindingBuilder
                .bind(dishCreatedQueue)
                .to(dishExchange)
                .with(DISH_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding dishUpdatedBinding(Queue dishUpdatedQueue, TopicExchange dishExchange) {
        return BindingBuilder
                .bind(dishUpdatedQueue)
                .to(dishExchange)
                .with(DISH_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding dishDeletedBinding(Queue dishDeletedQueue, TopicExchange dishExchange) {
        return BindingBuilder
                .bind(dishDeletedQueue)
                .to(dishExchange)
                .with(DISH_DELETED_ROUTING_KEY);
    }

    @Bean
    public Binding dishAvailabilityChangedBinding(
            Queue dishAvailabilityChangedQueue,
            TopicExchange dishExchange) {
        return BindingBuilder
                .bind(dishAvailabilityChangedQueue)
                .to(dishExchange)
                .with(DISH_AVAILABILITY_CHANGED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());

        // Retry configuration
        template.setRetryTemplate(new org.springframework.retry.support.RetryTemplate());

        return template;
    }
}
