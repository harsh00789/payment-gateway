package com.razorpay.payment.controller;

import com.razorpay.payment.entity.PaymentTransaction;
import com.razorpay.payment.service.IdempotencyService;
import com.razorpay.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/payments")
    public ResponseEntity<?> getAllPayments(){
        List<PaymentTransaction> paymentTransactions = paymentService.getAllPayments();
        return ResponseEntity.status(HttpStatus.OK).body(paymentTransactions);
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<?> getPayment(@PathVariable Long id){
     PaymentTransaction paymentTransaction = paymentService.getPayment(id);
        return ResponseEntity.status(HttpStatus.OK).body(paymentTransaction);
    }

    @GetMapping("/payments/stats")
    public ResponseEntity<?> getStats(){
        Map<String , Long> stats = paymentService.getStats();
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }
}
