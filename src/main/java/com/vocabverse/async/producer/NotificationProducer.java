package com.vocabverse.async.producer;

import com.vocabverse.async.config.RabbitMqConfig;
import com.vocabverse.async.event.ReviewDueEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishReviewDueEvent(ReviewDueEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.VOCABVERSE_EXCHANGE,
                RabbitMqConfig.NOTIFICATION_EMAIL_ROUTING_KEY,
                event
        );
    }
}
