package com.razorpay.payment.service;

import com.razorpay.payment.entity.WebhookEvent;
import com.razorpay.payment.repository.WebhookEventRepository;
import com.razorpay.payment.util.HmacUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpResponse;
import java.util.List;

@Service
public class WebhookService {

    private final String WEBHOOK_SECRET = "super-secret";
    private final WebhookEventRepository webhookEventRepository;

    @Autowired
    public WebhookService(WebhookEventRepository webhookEventRepository) {
        this.webhookEventRepository = webhookEventRepository;
    }

    public void createWebhook(String webhookUrl,String eventType,String payload){

        WebhookEvent.WebhookEventBuilder webhookEventBuilder = WebhookEvent.builder()
                .eventType(eventType)
                .payload(payload)
                .webhookUrl(webhookUrl)
                .delivered(false)
                .retryCount(0);

        WebhookEvent webhookEvent= webhookEventBuilder.build();

        webhookEventRepository.save(webhookEvent);
    }

    @Async
    public void deliverWebhook(WebhookEvent event){
        try{
            String signature =    HmacUtil.generateHmac(WEBHOOK_SECRET,event.getPayload());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Signature",signature);

            HttpEntity<String> httpEntity = new HttpEntity<>(event.getPayload(),headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response =  restTemplate.exchange(
                    event.getWebhookUrl(),
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );

            event.setDelivered(true);
            webhookEventRepository.save(event);

        }catch (Exception e){
            event.setRetryCount(event.getRetryCount()+1);
            webhookEventRepository.save(event);
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void webhookScheduling(){
      List<WebhookEvent> events =  webhookEventRepository.findAll()
                .stream().filter(event -> !event.isDelivered() && event.getRetryCount()<5)
                .toList();
      for(WebhookEvent event : events){
          deliverWebhook(event);
      }
    }
}
