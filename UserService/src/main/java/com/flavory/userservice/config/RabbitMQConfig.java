package com.flavory.userservice.config;

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

    public static final String USER_EXCHANGE = "user.events";
    public static final String USER_UPDATED_QUEUE = "user.updated.queue";
    public static final String USER_UPDATED_ROUTING_KEY = "user.updated";

    public static final String DLX_EXCHANGE = "dlx.exchange";

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
    public Queue userUpdatedQueue() {
        return QueueBuilder
                .durable(USER_UPDATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.user.updated")
                .build();
    }

    @Bean
    public Binding userUpdatedBinding(Queue userUpdatedQueue, TopicExchange userExchange) {
        return BindingBuilder
                .bind(userUpdatedQueue)
                .to(userExchange)
                .with(USER_UPDATED_ROUTING_KEY);
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
