package com.ecommerce.orderevent.dtos;

import com.ecommerce.orderevent.entity.Order;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderResponseDto {
    private Long id;
    private LocalDateTime orderDate;
    private String status;
    private Double totalPrice;

    private UserResponseDto user;
    private RestaurantResponseDto restaurant;
    private List<MenuItemResponseDto> items;

    public static OrderResponseDto fromEntity(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());

        if (order.getUser() != null) {
            dto.setUser(UserResponseDto.fromEntity(order.getUser()));
        }

        if (order.getRestaurant() != null) {
            dto.setRestaurant(RestaurantResponseDto.fromEntity(order.getRestaurant()));
        }

        if (order.getItems() != null) {
            dto.setItems(order.getItems()
                    .stream()
                    .map(MenuItemResponseDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
