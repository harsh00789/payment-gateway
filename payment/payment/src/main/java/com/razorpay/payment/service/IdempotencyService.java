package com.razorpay.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private final Duration TTL = Duration.ofMinutes(10);
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public IdempotencyService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

//    public boolean isDuplicate(String key){
//      Boolean exist =  stringRedisTemplate.hasKey(key);
//      if(Boolean.TRUE == exist){
//          return true;
//      }
//
//      stringRedisTemplate.opsForValue().set(key,"processed",TTL);
//      return false;
//    }

    public String getExistingValue(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void saveValue(String key,String value){
        stringRedisTemplate.opsForValue().set(key,value,TTL);
    }
}
