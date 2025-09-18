package com.ecommerce.orderevent.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private List<Long> menuItemIds;
    private String status; // CREATED, PAID, FAILED, DELIVERED
}
