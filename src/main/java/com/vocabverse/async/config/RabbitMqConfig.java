package com.vocabverse.async.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String VOCABVERSE_EXCHANGE = "vocabverse.exchange";
    public static final String NOTIFICATION_EMAIL_QUEUE = "notification.email.queue";
    public static final String NOTIFICATION_EMAIL_ROUTING_KEY = "notification.email.review-due";

    @Bean
    public DirectExchange vocabverseExchange() {
        return new DirectExchange(VOCABVERSE_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationEmailQueue() {
        return new Queue(NOTIFICATION_EMAIL_QUEUE, true);
    }

    @Bean
    public Binding notificationEmailBinding(Queue notificationEmailQueue, DirectExchange vocabverseExchange) {
        return BindingBuilder
                .bind(notificationEmailQueue)
                .to(vocabverseExchange)
                .with(NOTIFICATION_EMAIL_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
