package com.ecommerce.orderevent.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.*;
@Entity
@Data
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One User → Many Orders
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // One Restaurant → Many Orders
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // One Order → Many MenuItems (many-to-many relationship)
    @ManyToMany
    @JoinTable(
            name = "order_items",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_item_id")
    )
    private List<MenuItem> items;
    private LocalDateTime orderDate;
    private String status;  // e.g., PLACED, CONFIRMED, DELIVERED, CANCELLED
    private Double totalPrice;
}
