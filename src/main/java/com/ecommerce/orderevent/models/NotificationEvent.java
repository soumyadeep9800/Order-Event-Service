package com.ecommerce.orderevent.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private String message;
    private String type;     // ORDER, PAYMENT, DELIVERY
    private Long orderId;
}
