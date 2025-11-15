## Kafka-Workflow
```
Kafka-Wrokflow
[Client: Place Order] 
        |
        | POST /orders (saves order)
        v
[Order Service] -- save --> (DB)
        |
        | create OrderEvent(status=PLACED)
        | kafkaTemplate.send("order-events", event)
        v
[KAFKA BROKER: topic=order-events]
        |
        | (message persisted)
        v
[Notification Service - Kafka Consumer]
 @KafkaListener(topics="order-events")
        |
        | processNotification(event)
        |  - fetch user, restaurant, items
        |  - build HTML emails (Accept/Reject links)
        |  - send emails via EmailService
        v
[Restaurant Email (Accept / Reject links)]
        |
  Restaurant clicks Accept -> HTTP PUT /order/{id}/accept  (Order Service)
        |                                                       |
        |                                                       v
        |                                                 OrderService updates status -> save
        |                                                       |
        |                                                       | publish OrderEvent(status=ACCEPTED)
        |                                                       v
        +-----------------------------------------------> [KAFKA BROKER: order-events]
                                                                |
                                                                v
                                              [Notification Service consumes ACCEPTED]
                                                                |
                                           -> send payment link email to user
                                                                |
                             User clicks pay -> PaymentService handles payment
                                                                |
                                          PaymentService publishes PAYMENT_SUCCESS event
                                                                v
                                              [Notification Service consumes PAYMENT_SUCCESS]
                                                                |
                                                -> send payment-success email to user
```
