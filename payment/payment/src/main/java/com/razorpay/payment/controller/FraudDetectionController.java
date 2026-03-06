package com.razorpay.payment.controller;

import com.razorpay.payment.entity.FraudLog;
import com.razorpay.payment.entity.PaymentTransaction;
import com.razorpay.payment.service.FraudDetectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FraudDetectionController {
    private final FraudDetectionService fraudDetectionService;

    public FraudDetectionController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @GetMapping("/frauds")
    public ResponseEntity<?> getAllFrauds(){
        List<FraudLog> fraudLogs = fraudDetectionService.getAllFrauds();
        return ResponseEntity.status(HttpStatus.OK).body(fraudLogs);
    }
}
