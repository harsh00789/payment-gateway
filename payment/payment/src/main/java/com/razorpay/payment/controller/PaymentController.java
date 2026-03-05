package com.razorpay.payment.controller;

import com.razorpay.payment.entity.PaymentTransaction;
import com.razorpay.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process-payment/{orderId}")
    public ResponseEntity<PaymentTransaction> processPayment(@PathVariable Long orderId){
      PaymentTransaction paymentTransaction = paymentService.processPayment(orderId);
      return ResponseEntity.status(HttpStatus.OK).body(paymentTransaction);
    }
}
