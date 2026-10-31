package com.parksync.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.parksync.backend.dto.request.ReservationRequest;
import com.parksync.backend.dto.response.ReservationResponse;
import com.parksync.backend.dto.response.SlotAvailabilityResponse;
import com.parksync.backend.entity.AppUser;
import com.parksync.backend.entity.ParkingLot;
import com.parksync.backend.entity.ParkingSlot;
import com.parksync.backend.entity.Reservation;
import com.parksync.backend.entity.Vehicle;
import com.parksync.backend.exception.BadRequestException;
import com.parksync.backend.exception.ConflictException;
import com.parksync.backend.exception.ResourceNotFoundException;
import com.parksync.backend.repository.ParkingSlotRepository;
import com.parksync.backend.repository.ReservationRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final AppUserService appUserService;
    private final VehicleService vehicleService;
    private final ParkingLotService parkingLotService;

    public ReservationService(
            ReservationRepository reservationRepository,
            ParkingSlotRepository parkingSlotRepository,
            AppUserService appUserService,
            VehicleService vehicleService,
            ParkingLotService parkingLotService) {
        this.reservationRepository = reservationRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.appUserService = appUserService;
        this.vehicleService = vehicleService;
        this.parkingLotService = parkingLotService;
    }

    public Reservation getRequiredReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationDetails(Long reservationId) {
        return ReservationResponse.fromEntity(getRequiredReservationById(reservationId));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(Long userId) {
        appUserService.getRequiredUserById(userId);
        return reservationRepository.findByUserUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getSlotReservations(Long slotId) {
        parkingSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with ID: " + slotId));
        return reservationRepository.findBySlotSlotIdOrderByStartTimeDesc(slotId).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ReservationResponse createReservation(ReservationRequest request) {
        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();

        if (start == null || end == null) {
            throw new BadRequestException("Start time and end time are required");
        }

        if (!end.isAfter(start)) {
            throw new BadRequestException("Reservation end time must be later than start time");
        }

        // Validate user
        AppUser user = appUserService.getRequiredUserById(request.getUserId());
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("User account is deactivated");
        }

        // Validate vehicle
        Vehicle vehicle = vehicleService.getRequiredVehicleById(request.getVehicleId());
        if (!Boolean.TRUE.equals(vehicle.getIsActive())) {
            throw new BadRequestException("Vehicle is deactivated");
        }
        if (!vehicle.getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Vehicle does not belong to the requesting user");
        }

        // Acquire pessimistic write lock on slot to prevent concurrent double-booking
        ParkingSlot slot = parkingSlotRepository.findByIdWithLock(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with ID: " + request.getSlotId()));

        if (!Boolean.TRUE.equals(slot.getIsActive())) {
            throw new BadRequestException("Selected parking slot is inactive");
        }

        if ("MAINTENANCE".equalsIgnoreCase(slot.getStatus())) {
            throw new BadRequestException("Selected parking slot is under maintenance");
        }

        ParkingLot lot = slot.getParkingLot();
        if (lot == null || !Boolean.TRUE.equals(lot.getIsActive())) {
            throw new BadRequestException("The parking lot for this slot is inactive");
        }

        // Double-booking prevention: overlap query
        List<Reservation> overlaps = reservationRepository.findOverlappingReservations(slot.getSlotId(), start, end);
        if (!overlaps.isEmpty()) {
            throw new ConflictException("Slot " + slot.getSlotNumber() + " is already booked for the requested time period (" +
                    start + " to " + end + ")");
        }

        // Calculate amount based on hourly rate
        BigDecimal amount = calculatePrice(lot.getPricePerHour(), start, end);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setVehicle(vehicle);
        reservation.setSlot(slot);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus("PENDING");
        reservation.setTotalAmount(amount);

        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.fromEntity(saved);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long reservationId, Long requestingUserId, boolean isAdmin) {
        Reservation reservation = getRequiredReservationById(reservationId);

        if (!isAdmin && requestingUserId != null && !reservation.getUser().getUserId().equals(requestingUserId)) {
            throw new BadRequestException("You can only cancel your own reservations");
        }

        if ("CANCELLED".equalsIgnoreCase(reservation.getStatus())) {
            throw new BadRequestException("Reservation is already cancelled");
        }

        if ("COMPLETED".equalsIgnoreCase(reservation.getStatus())) {
            throw new BadRequestException("Cannot cancel a completed reservation");
        }

        reservation.setStatus("CANCELLED");
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.fromEntity(saved);
    }

    @Transactional
    public ReservationResponse completeReservation(Long reservationId) {
        Reservation reservation = getRequiredReservationById(reservationId);

        if ("CANCELLED".equalsIgnoreCase(reservation.getStatus())) {
            throw new BadRequestException("Cannot complete a cancelled reservation");
        }

        reservation.setStatus("COMPLETED");
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SlotAvailabilityResponse> checkLotSlotsAvailability(Long lotId, LocalDateTime start, LocalDateTime end) {
        ParkingLot lot = parkingLotService.getRequiredParkingLotById(lotId);
        List<ParkingSlot> slots = parkingSlotRepository.findByLotIdAndIsActiveTrue(lot.getLotId());

        List<SlotAvailabilityResponse> results = new ArrayList<>();
        for (ParkingSlot s : slots) {
            if ("MAINTENANCE".equalsIgnoreCase(s.getStatus())) {
                results.add(new SlotAvailabilityResponse(s.getSlotId(), s.getSlotNumber(), s.getSlotType(), false, "MAINTENANCE"));
                continue;
            }

            boolean isAvailable = true;
            if (start != null && end != null && end.isAfter(start)) {
                List<Reservation> overlaps = reservationRepository.findOverlappingReservations(s.getSlotId(), start, end);
                isAvailable = overlaps.isEmpty();
            } else {
                isAvailable = "AVAILABLE".equalsIgnoreCase(s.getStatus());
            }

            String displayStatus = isAvailable ? "AVAILABLE" : "RESERVED";
            results.add(new SlotAvailabilityResponse(s.getSlotId(), s.getSlotNumber(), s.getSlotType(), isAvailable, displayStatus));
        }

        return results;
    }

    public BigDecimal calculatePrice(BigDecimal hourlyRate, LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        long totalMinutes = duration.toMinutes();

        if (totalMinutes <= 0) {
            throw new BadRequestException("Invalid reservation duration");
        }

        // Bill by ceil hours, minimum 1 hour
        double hours = Math.max(1.0, Math.ceil(totalMinutes / 60.0));
        return hourlyRate.multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP);
    }
}
