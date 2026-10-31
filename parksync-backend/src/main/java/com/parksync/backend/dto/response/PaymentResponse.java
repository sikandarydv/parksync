package com.parksync.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.parksync.backend.entity.Payment;

public class PaymentResponse {

    private Long paymentId;
    private Long reservationId;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionReference;
    private LocalDateTime paymentTime;
    private String reservationStatus;

    public PaymentResponse() {
    }

    public static PaymentResponse fromEntity(Payment p) {
        if (p == null) return null;
        PaymentResponse dto = new PaymentResponse();
        dto.setPaymentId(p.getPaymentId());
        if (p.getReservation() != null) {
            dto.setReservationId(p.getReservation().getReservationId());
            dto.setReservationStatus(p.getReservation().getStatus());
        }
        dto.setAmount(p.getAmount());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setPaymentStatus(p.getPaymentStatus());
        dto.setTransactionReference(p.getTransactionReference());
        dto.setPaymentTime(p.getPaymentTime());
        return dto;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}
