
package com.razorpay.payment.controller;

import com.razorpay.payment.BO.OrderRequest;
import com.razorpay.payment.BO.OrderResponse;
import com.razorpay.payment.entity.PaymentOrder;
import com.razorpay.payment.entity.PaymentTransaction;
import com.razorpay.payment.service.OrderService;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

     @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestHeader String idempotencyKey, @RequestBody OrderRequest orderRequest) {
       OrderResponse orderResponse = orderService.createOrder(idempotencyKey, orderRequest);
       return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(){
        List<PaymentOrder> paymentOrders = orderService.getAllOrders();
        return ResponseEntity.status(HttpStatus.OK).body(paymentOrders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id){
        PaymentOrder paymentOrder = orderService.getOrder(id);
        return ResponseEntity.status(HttpStatus.OK).body(paymentOrder);
    }
}
