package com.razorpay.payment.service;

import com.razorpay.payment.OrderStatus;
import com.razorpay.payment.entity.PaymentOrder;
import com.razorpay.payment.entity.PaymentTransaction;
import com.razorpay.payment.repository.PaymentOrderRepository;
import com.razorpay.payment.repository.PaymentTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private  final WebhookService webhookService;

    @Autowired
    public PaymentService(PaymentOrderRepository paymentOrderRepository, PaymentTransactionRepository paymentTransactionRepository, WebhookService webhookService) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.webhookService = webhookService;
    }

    @Transactional
    public PaymentTransaction processPayment(Long orderId){
      PaymentOrder paymentOrder = paymentOrderRepository.findById(orderId)
                .orElseThrow(()->new RuntimeException("order not found"));

      if (paymentOrder.getStatus() != OrderStatus.CREATED){
          throw new RuntimeException("payment already created");
      }

      paymentOrder.setStatus(OrderStatus.PROCESSING);

      Boolean isProcessed = new Random().nextBoolean();
      String status = isProcessed?"Success":"failed";
        PaymentTransaction.PaymentTransactionBuilder paymentTransactionBuilder = PaymentTransaction.builder()
                .gatewayResponse("gateway response")
                .order(paymentOrder)
                .status(status);

        paymentOrder.setStatus(isProcessed?OrderStatus.COMPLETED:OrderStatus.FAILED);

        String eventType = isProcessed?"success":"failed";
        String payload = """
                {
                "orderId":"%s",
                "status":"%s"
                }
                """.formatted(paymentOrder.getId(),status);


 webhookService.createWebhook(
         "https://webhook.site/69aa335d-04b1-4bd9-b4d9-4913ca606776",
         eventType,
         payload
 );
        return paymentTransactionRepository.save(paymentTransactionBuilder.build());

    }
}
