package com.parksync.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parksync.backend.dto.request.PaymentRequest;
import com.parksync.backend.dto.response.PaymentResponse;
import com.parksync.backend.entity.Payment;
import com.parksync.backend.entity.Reservation;
import com.parksync.backend.exception.BadRequestException;
import com.parksync.backend.exception.ConflictException;
import com.parksync.backend.exception.ResourceNotFoundException;
import com.parksync.backend.repository.PaymentRepository;
import com.parksync.backend.repository.ReservationRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final EmailService emailService;

    public PaymentService(
            PaymentRepository paymentRepository, 
            ReservationRepository reservationRepository,
            EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.emailService = emailService;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + request.getReservationId()));

        if ("CANCELLED".equalsIgnoreCase(reservation.getStatus())) {
            throw new BadRequestException("Cannot pay for a cancelled reservation");
        }

        // Check if payment already exists
        Payment payment = paymentRepository.findByReservationReservationId(reservation.getReservationId())
                .orElse(null);

        if (payment != null && "SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new ConflictException("Payment has already been completed for this reservation");
        }

        if (payment == null) {
            payment = new Payment();
            payment.setReservation(reservation);
            payment.setAmount(reservation.getTotalAmount());
        }

        payment.setPaymentMethod(request.getPaymentMethod().trim().toUpperCase());
        payment.setPaymentTime(LocalDateTime.now());

        if (request.isSimulateFailure()) {
            payment.setPaymentStatus("FAILED");
            payment.setTransactionReference("FAIL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } else {
            payment.setPaymentStatus("SUCCESS");
            payment.setTransactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
            
            // Mark reservation CONFIRMED
            reservation.setStatus("CONFIRMED");
            reservationRepository.save(reservation);
        }

        Payment saved = paymentRepository.save(payment);

        if ("SUCCESS".equalsIgnoreCase(saved.getPaymentStatus())) {
            emailService.sendBookingConfirmationEmail(reservation, saved);
        }

        return PaymentResponse.fromEntity(saved);
    }

    public PaymentResponse getPaymentByReservationId(Long reservationId) {
        Payment payment = paymentRepository.findByReservationReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for reservation ID: " + reservationId));
        return PaymentResponse.fromEntity(payment);
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        return PaymentResponse.fromEntity(payment);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAllByOrderByPaymentTimeDesc().stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
