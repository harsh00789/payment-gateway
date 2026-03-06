package com.razorpay.payment.service;

import com.razorpay.payment.BO.PaymentEvent;
import com.razorpay.payment.OrderStatus;
import com.razorpay.payment.entity.FraudLog;
import com.razorpay.payment.entity.PaymentOrder;
import com.razorpay.payment.entity.PaymentTransaction;
import com.razorpay.payment.repository.PaymentOrderRepository;
import com.razorpay.payment.repository.PaymentTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private  final WebhookService webhookService;
    private final IdempotencyService idempotencyService;
    private final FraudDetectionService fraudDetectionService;
    private final PaymentEventPublisherService paymentEventPublisherService;

    @Autowired
    public PaymentService(PaymentOrderRepository paymentOrderRepository, PaymentTransactionRepository paymentTransactionRepository, WebhookService webhookService, IdempotencyService idempotencyService, FraudDetectionService fraudDetectionService, PaymentEventPublisherService paymentEventPublisherService) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.webhookService = webhookService;
        this.idempotencyService = idempotencyService;
        this.fraudDetectionService = fraudDetectionService;
        this.paymentEventPublisherService = paymentEventPublisherService;
    }

    @Transactional
    public PaymentTransaction processPayment(String idempotency,Long orderId){

        String key = "payment:idempotency"+idempotency;

        String existingPaymentId = idempotencyService.getExistingValue(key);
        if(Objects.nonNull(existingPaymentId)){
            return paymentTransactionRepository.findById(Long.parseLong(existingPaymentId)).orElseThrow();
        }

      PaymentOrder paymentOrder = paymentOrderRepository.findById(orderId)
                .orElseThrow(()->new RuntimeException("order not found"));

      if (paymentOrder.getStatus() != OrderStatus.CREATED){
          throw new RuntimeException("payment already created");
      }

if(fraudDetectionService.isFraud(orderId,paymentOrder.getAmount().longValue())){
    throw new RuntimeException("Fraudulent transaction detected");
}
      paymentOrder.setStatus(OrderStatus.PROCESSING);

      Boolean isProcessed = new Random().nextBoolean();
      String status = isProcessed?"Success":"failed";
        PaymentTransaction.PaymentTransactionBuilder paymentTransactionBuilder = PaymentTransaction.builder()
                .gatewayResponse("gateway response")
                .order(paymentOrder)
                .status(status);

        paymentOrder.setStatus(isProcessed?OrderStatus.COMPLETED:OrderStatus.FAILED);

//        String eventType = isProcessed?"success":"failed";
//        String payload = """
//                {
//                "orderId":"%s",
//                "status":"%s"
//                }
//                """.formatted(paymentOrder.getId(),status);
//
//
// webhookService.createWebhook(
//         "https://webhook.site/69aa335d-04b1-4bd9-b4d9-4913ca606776",
//         eventType,
//         payload
// );



        PaymentTransaction paymentTransaction = paymentTransactionRepository.save(paymentTransactionBuilder.build());

        idempotencyService.saveValue(key,paymentTransaction.getId().toString());
        PaymentEvent.PaymentEventBuilder paymentEventBuilder = PaymentEvent.builder()
                .paymentId(paymentTransaction.getId())
                .orderId(orderId)
                .status(status)
                .amount(paymentOrder.getAmount());

        paymentEventPublisherService.publish(paymentEventBuilder.build());
        return paymentTransaction;
    }



    public List<PaymentTransaction> getAllPayments(){
        List<PaymentTransaction> paymentTransactions =  paymentTransactionRepository.findAll();
        return paymentTransactions;
    }

    public PaymentTransaction getPayment(Long id){
        PaymentTransaction paymentTransaction =  paymentTransactionRepository.findById(id).orElseThrow();
        return paymentTransaction;
    }


    public Map<String, Long> getStats(){
      Long paymentOrders =  paymentOrderRepository.count();
        Long paymentTransactions =  paymentTransactionRepository.count();
        Map<String,Long> stats = new HashMap<>();
        stats.put("totalOrders",paymentOrders);
        stats.put("totalPayments",paymentTransactions);
        return stats;
    }

}
