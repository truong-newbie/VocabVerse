package com.vocabverse.async.consumer;

import com.vocabverse.async.config.RabbitMqConfig;
import com.vocabverse.async.event.ReviewDueEvent;
import com.vocabverse.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(
            queues = RabbitMqConfig.NOTIFICATION_EMAIL_QUEUE,
            autoStartup = "${notification.email.listener.enabled:true}"
    )
    public void consumeReviewDueEvent(ReviewDueEvent event) {
        notificationService.sendReviewDueEmail(event);
    }
}
