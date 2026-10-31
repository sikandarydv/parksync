package com.parksync.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.parksync.backend.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.slot.slotId = :slotId " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND r.startTime < :requestedEndTime " +
           "AND r.endTime > :requestedStartTime")
    List<Reservation> findOverlappingReservations(
            @Param("slotId") Long slotId,
            @Param("requestedStartTime") LocalDateTime requestedStartTime,
            @Param("requestedEndTime") LocalDateTime requestedEndTime);

    List<Reservation> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<Reservation> findBySlotSlotIdOrderByStartTimeDesc(Long slotId);

    List<Reservation> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);
}
