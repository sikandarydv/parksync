package com.parksync.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parksync.backend.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByUserId(Long userId);
    List<Vehicle> findByUserIdAndIsActiveTrue(Long userId);
    boolean existsByVehicleNumber(String vehicleNumber);
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}
