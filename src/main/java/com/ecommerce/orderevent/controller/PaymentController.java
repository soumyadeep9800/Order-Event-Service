package com.ecommerce.orderevent.controller;

import com.ecommerce.orderevent.dtos.ApiResponse;
import com.ecommerce.orderevent.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static com.ecommerce.orderevent.constants.ApiResponseStatus.SUCCESS;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService){
        this.paymentService=paymentService;
    }
    @GetMapping("/payments/{orderId}/pay")
    public ResponseEntity<ApiResponse<String>> simulatePayment(@PathVariable Long orderId) {
        String status = paymentService.initiatePayment(orderId);
        ApiResponse<String> response = new ApiResponse<>(
                SUCCESS,
                "Payment successfully!",
                status,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }
}
