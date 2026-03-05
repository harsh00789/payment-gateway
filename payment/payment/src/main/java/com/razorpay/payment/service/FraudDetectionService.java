package com.razorpay.payment.service;

import com.razorpay.payment.entity.FraudLog;
import com.razorpay.payment.repository.FraudLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class FraudDetectionService {
    private final IdempotencyService idempotencyService;
    private final RedisTemplate redisTemplate;
    private final FraudLogRepository fraudLogRepository;

    @Autowired
    public FraudDetectionService(IdempotencyService idempotencyService, RedisTemplate redisTemplate, FraudLogRepository fraudLogRepository) {
        this.idempotencyService = idempotencyService;
        this.redisTemplate = redisTemplate;
        this.fraudLogRepository = fraudLogRepository;
    }

    public boolean isFraud(Long orderId, Long amount){
        String key = "fraud-detect "+orderId;

       Long count =  redisTemplate.opsForValue().increment(key);
       if(Objects.nonNull(count) & count>5){
           FraudLog.FraudLogBuilder fraudLogBuilder =  FraudLog.builder()
                   .amount(BigDecimal.valueOf(amount))
                   .reason("multiple payment attempts")
                   .orderId(orderId);

           fraudLogRepository.save(fraudLogBuilder.build());
           return true;
       }

       if(amount>100000){
           FraudLog.FraudLogBuilder fraudLogBuilder =  FraudLog.builder()
                   .amount(BigDecimal.valueOf(amount))
                   .reason("amount is greater than 100000")
                   .orderId(orderId);

           fraudLogRepository.save(fraudLogBuilder.build());
           return true;
       }
       return false;
    }
}
