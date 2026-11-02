package com.parksync.backend.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parksync.backend.dto.response.PaymentResponse;
import com.parksync.backend.dto.response.ReservationResponse;
import com.parksync.backend.dto.response.UserResponse;
import com.parksync.backend.entity.Reservation;
import com.parksync.backend.repository.AppUserRepository;
import com.parksync.backend.repository.ParkingLotRepository;
import com.parksync.backend.repository.ParkingSlotRepository;
import com.parksync.backend.repository.PaymentRepository;
import com.parksync.backend.repository.ReservationRepository;
import com.parksync.backend.service.AppUserService;
import com.parksync.backend.service.EmailService;
import com.parksync.backend.service.PaymentService;
import com.parksync.backend.service.ReservationService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final AppUserRepository appUserRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final AppUserService appUserService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final EmailService emailService;

    public AdminController(
            ParkingLotRepository parkingLotRepository,
            ParkingSlotRepository parkingSlotRepository,
            AppUserRepository appUserRepository,
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            AppUserService appUserService,
            ReservationService reservationService,
            PaymentService paymentService,
            EmailService emailService) {
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.appUserRepository = appUserRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.appUserService = appUserService;
        this.reservationService = reservationService;
        this.paymentService = paymentService;
        this.emailService = emailService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalLots = parkingLotRepository.count();
        long totalSlots = parkingSlotRepository.count();
        long totalUsers = appUserRepository.count();
        long totalReservations = reservationRepository.count();
        long activeReservations = reservationRepository.countByStatus("CONFIRMED");
        long pendingReservations = reservationRepository.countByStatus("PENDING");
        long successfulPayments = paymentRepository.countByPaymentStatus("SUCCESS");

        BigDecimal totalRevenue = paymentRepository.findAll().stream()
                .filter(p -> "SUCCESS".equalsIgnoreCase(p.getPaymentStatus()))
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalLots", totalLots);
        stats.put("totalSlots", totalSlots);
        stats.put("totalUsers", totalUsers);
        stats.put("totalReservations", totalReservations);
        stats.put("activeReservations", activeReservations);
        stats.put("pendingReservations", pendingReservations);
        stats.put("successfulPayments", successfulPayments);
        stats.put("totalRevenue", totalRevenue);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(appUserService.getAllUsers());
    }

    @PatchMapping("/users/{id}/toggle-active")
    public ResponseEntity<UserResponse> toggleUserActive(@PathVariable Long id, @RequestParam boolean isActive) {
        UserResponse response = appUserService.toggleUserActive(id, isActive);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @PostMapping("/send-test-alert")
    public ResponseEntity<Map<String, Object>> sendTestAlert(@RequestParam Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new com.parksync.backend.exception.ResourceNotFoundException("Reservation not found: " + reservationId));
        emailService.sendExpiryAlertEmail(res);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Test alert email dispatched for reservation #" + reservationId + " to " + res.getUser().getEmail()
        ));
    }
}
