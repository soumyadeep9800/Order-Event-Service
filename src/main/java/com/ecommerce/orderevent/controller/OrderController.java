package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.entity.Order;
import com.ecommerce.orderevent.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;
    public  OrderController(OrderService orderService){
        this.orderService=orderService;
    }

//    @PostMapping("/place-order")
//    public Order placeOrder(){
//
//    }
}
