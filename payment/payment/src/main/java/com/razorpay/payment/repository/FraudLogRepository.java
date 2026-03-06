package com.razorpay.payment.repository;

import com.razorpay.payment.entity.FraudLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudLogRepository extends JpaRepository<FraudLog,Long> {
}
