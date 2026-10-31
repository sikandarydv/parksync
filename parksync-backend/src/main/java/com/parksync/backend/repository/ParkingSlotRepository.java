package com.parksync.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.parksync.backend.entity.ParkingSlot;

import jakarta.persistence.LockModeType;

public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    List<ParkingSlot> findByLotId(Long lotId);
    List<ParkingSlot> findByLotIdAndIsActiveTrue(Long lotId);
    boolean existsByLotIdAndSlotNumber(Long lotId, String slotNumber);
    Optional<ParkingSlot> findByLotIdAndSlotNumber(Long lotId, String slotNumber);
    long countByLotIdAndIsActiveTrue(Long lotId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ParkingSlot s WHERE s.slotId = :slotId")
    Optional<ParkingSlot> findByIdWithLock(@Param("slotId") Long slotId);
}
