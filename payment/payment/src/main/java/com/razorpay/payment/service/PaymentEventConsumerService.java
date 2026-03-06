package com.razorpay.payment.service;

import com.razorpay.payment.BO.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Objects;

@Slf4j
@Service
public class PaymentEventConsumerService {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String QUEUE_NAME = "event_queue";
    private final ObjectMapper objectMapper;
    private final WebhookService webhookService;

    @Autowired
    public PaymentEventConsumerService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper, WebhookService webhookService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.webhookService = webhookService;
    }

    @PostMapping
    public void startConsumer(){
        new Thread(()->{
            while (true){
                try {
                    String json = stringRedisTemplate.opsForList().leftPop(QUEUE_NAME, Duration.ofSeconds(5));
                    if (Objects.isNull(json)) {
                        continue;
                    }
                    PaymentEvent event = objectMapper.readValue(json, PaymentEvent.class);

                    String payload = """
                            {
                            "orderId":"%s",
                            "status":"%s"
                            }
                            """.formatted(event.getOrderId(), event.getStatus());


                    webhookService.createWebhook(
                            "https://webhook.site/69aa335d-04b1-4bd9-b4d9-4913ca606776",
                            event.getStatus(),
                            payload
                    );
                }catch (Exception e){
                    log.info("error processing event : "+e);
                }

            }
        }).start();
    }
}
