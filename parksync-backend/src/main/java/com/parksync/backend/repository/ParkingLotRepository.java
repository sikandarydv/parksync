package com.parksync.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parksync.backend.entity.ParkingLot;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    List<ParkingLot> findByIsActiveTrue();
}
