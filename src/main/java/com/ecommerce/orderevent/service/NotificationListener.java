package com.ecommerce.orderevent.service;

import com.ecommerce.orderevent.models.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void consume(OrderEvent event) {
        log.info("📩 Received order event: {}", event);
        notificationService.processNotification(event);
    }
}



