package com.razorpay.payment.repository;

import com.razorpay.payment.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent,Long> {
}
