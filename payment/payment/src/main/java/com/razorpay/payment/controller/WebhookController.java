package com.razorpay.payment.controller;

import com.razorpay.payment.entity.WebhookEvent;
import com.razorpay.payment.service.WebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/webhook")
public class WebhookController {
    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllWebhooks(){
        List<WebhookEvent> webhookEvents = webhookService.getAllWebhooks();
        return ResponseEntity.status(HttpStatus.OK).body(webhookEvents);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifySignature(@RequestHeader("X-Signature") String signature, @RequestBody String payload){
       boolean isVerified = webhookService.handleWebhook(signature,payload);
       if(!isVerified){
           return ResponseEntity.status(401).build();
       }

       return ResponseEntity.status(HttpStatus.OK).body("verified");
    }
}
