package com.parksync.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parksync.backend.dto.request.ExtendReservationRequest;
import com.parksync.backend.dto.request.ReservationRequest;
import com.parksync.backend.dto.response.ReservationResponse;
import com.parksync.backend.dto.response.SlotAvailabilityResponse;
import com.parksync.backend.service.ReservationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        // If authenticated, override or verify userId
        if (currentUserId != null && request.getUserId() == null) {
            request.setUserId(currentUserId);
        }
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        ReservationResponse response = reservationService.getReservationDetails(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public List<ReservationResponse> getUserReservations(@PathVariable Long userId) {
        return reservationService.getUserReservations(userId);
    }

    @GetMapping("/slot/{slotId}")
    public List<ReservationResponse> getSlotReservations(@PathVariable Long slotId) {
        return reservationService.getSlotReservations(slotId);
    }

    @GetMapping
    public List<ReservationResponse> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long id,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId,
            @RequestAttribute(value = "currentUserRole", required = false) String currentUserRole) {
        boolean isAdmin = "ROLE_ADMIN".equals(currentUserRole);
        ReservationResponse response = reservationService.cancelReservation(id, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ReservationResponse> completeReservation(@PathVariable Long id) {
        ReservationResponse response = reservationService.completeReservation(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<ReservationResponse> extendReservation(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ExtendReservationRequest request,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId,
            @RequestAttribute(value = "currentUserRole", required = false) String currentUserRole) {
        if (request == null) {
            request = new ExtendReservationRequest(1);
        }
        boolean isAdmin = "ROLE_ADMIN".equals(currentUserRole);
        ReservationResponse response = reservationService.extendReservation(id, request, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/check-extension")
    public ResponseEntity<?> checkExtensionAvailability(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int extraHours) {
        boolean available = reservationService.checkExtensionAvailable(id, extraHours);
        return ResponseEntity.ok(java.util.Map.of("available", available, "extraHours", extraHours));
    }

    @GetMapping("/availability")
    public List<SlotAvailabilityResponse> checkAvailability(
            @RequestParam Long lotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return reservationService.checkLotSlotsAvailability(lotId, startTime, endTime);
    }
}
