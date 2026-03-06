package com.razorpay.payment.service;

import com.razorpay.payment.BO.OrderRequest;
import com.razorpay.payment.BO.OrderResponse;
import com.razorpay.payment.OrderStatus;
import com.razorpay.payment.entity.PaymentOrder;
import com.razorpay.payment.entity.PaymentTransaction;
import com.razorpay.payment.repository.PaymentOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OrderService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final IdempotencyService idempotencyService;

    @Autowired
    public OrderService(PaymentOrderRepository paymentOrderRepository, IdempotencyService idempotencyService) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.idempotencyService = idempotencyService;
    }

    public OrderResponse createOrder(String idempotencyKey, OrderRequest orderRequest) {

        String key = "order:idempotency"+idempotencyKey;
        String existingOrderId = idempotencyService.getExistingValue(key);
        if(Objects.nonNull(existingOrderId)){
          PaymentOrder paymentOrder =  paymentOrderRepository.findById(Long.parseLong(existingOrderId)).orElseThrow();
        return mapTOOrder(paymentOrder);
        }
        PaymentOrder.PaymentOrderBuilder paymentOrderBuilder = PaymentOrder.builder()
                .amount(orderRequest.getAmount())
                .currency(orderRequest.getCurrency())
                .idempotencyKey(idempotencyKey)
                        .status(OrderStatus.CREATED);


       PaymentOrder paymentOrder =  paymentOrderRepository.save(paymentOrderBuilder.build());
       idempotencyService.saveValue(key,paymentOrder.getId().toString());
       return mapTOOrder(paymentOrder);
    }

    private OrderResponse mapTOOrder(PaymentOrder paymentOrder) {
        return OrderResponse.builder()
                .id(paymentOrder.getId())
                .amount(paymentOrder.getAmount())
                .status(paymentOrder.getStatus())
                .currency(paymentOrder.getCurrency())
                .build();
    }

    public List<PaymentOrder> getAllOrders(){
      List<PaymentOrder> paymentOrders =  paymentOrderRepository.findAll();
      return paymentOrders;
    }

    public PaymentOrder getOrder(Long id){
        PaymentOrder paymentOrder =  paymentOrderRepository.findById(id).orElseThrow();
        return paymentOrder;
    }
}
