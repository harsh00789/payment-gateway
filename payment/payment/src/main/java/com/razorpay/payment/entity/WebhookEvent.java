package com.razorpay.payment.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "webhoook_event")
public class WebhookEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String eventType;
    private String payload;
    private String webhookUrl;
    private boolean delivered;

    private int retryCount;


    @CreatedDate
    private LocalDate createdAt;

    @UpdateTimestamp
    private LocalDate updatedAt;
}
