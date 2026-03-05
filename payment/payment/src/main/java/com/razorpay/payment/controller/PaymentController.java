package com.razorpay.payment.controller;

import com.razorpay.payment.entity.PaymentTransaction;
import com.razorpay.payment.service.IdempotencyService;
import com.razorpay.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
    private final PaymentService paymentService;
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process-payment/{orderId}")
    public ResponseEntity<?> processPayment(@RequestHeader String idempotencyKey, @PathVariable Long orderId){
      PaymentTransaction paymentTransaction = paymentService.processPayment(idempotencyKey,orderId);
      return ResponseEntity.status(HttpStatus.OK).body(paymentTransaction);
    }
}
