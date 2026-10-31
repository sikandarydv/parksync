package com.parksync.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parksync.backend.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservationReservationId(Long reservationId);
    Optional<Payment> findByTransactionReference(String transactionReference);
    List<Payment> findAllByOrderByPaymentTimeDesc();
    long countByPaymentStatus(String paymentStatus);
}
