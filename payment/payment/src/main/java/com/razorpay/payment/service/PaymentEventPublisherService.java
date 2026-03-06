package com.razorpay.payment.service;

import com.razorpay.payment.BO.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class PaymentEventPublisherService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String QUEUE_NAME = "event_queue";
    private final ObjectMapper objectMapper;

    @Autowired
    public PaymentEventPublisherService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(PaymentEvent paymentEvent){

        try {
            String json = objectMapper.writeValueAsString(paymentEvent);
            stringRedisTemplate.opsForList().rightPush(QUEUE_NAME, json);
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
}
