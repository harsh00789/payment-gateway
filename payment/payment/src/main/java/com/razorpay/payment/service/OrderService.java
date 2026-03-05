package com.razorpay.payment.service;

import com.razorpay.payment.BO.OrderRequest;
import com.razorpay.payment.BO.OrderResponse;
import com.razorpay.payment.OrderStatus;
import com.razorpay.payment.entity.PaymentOrder;
import com.razorpay.payment.repository.PaymentOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderService {

    private final PaymentOrderRepository paymentOrderRepository;

    @Autowired
    public OrderService(PaymentOrderRepository paymentOrderRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
    }

    public OrderResponse createOrder(String idempotencyKey, OrderRequest orderRequest) {
        Optional<PaymentOrder> paymentOrderOptional = paymentOrderRepository.findByIdempotencyKey(idempotencyKey);
        if (paymentOrderOptional.isPresent()) {
            return mapTOOrder(paymentOrderOptional.get());
        }

        PaymentOrder.PaymentOrderBuilder paymentOrderBuilder = PaymentOrder.builder()
                .amount(orderRequest.getAmount())
                .currency(orderRequest.getCurrency())
                .idempotencyKey(idempotencyKey)
                        .status(OrderStatus.CREATED);


       PaymentOrder paymentOrder =  paymentOrderRepository.save(paymentOrderBuilder.build());
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
}
