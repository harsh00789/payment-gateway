package com.razorpay.payment.entity;

import com.razorpay.payment.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payment_transaction")
@EntityListeners(AuditingEntityListener.class)
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private PaymentOrder order;

    private String status;

    private String gatewayResponse;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime localDateTime;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedDate;
}
