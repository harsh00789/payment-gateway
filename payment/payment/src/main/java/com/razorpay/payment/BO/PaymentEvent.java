package com.razorpay.payment.BO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {
    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private String status;
}
