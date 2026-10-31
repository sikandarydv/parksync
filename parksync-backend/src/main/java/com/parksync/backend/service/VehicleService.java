package com.parksync.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parksync.backend.dto.request.VehicleRequest;
import com.parksync.backend.entity.AppUser;
import com.parksync.backend.entity.Vehicle;
import com.parksync.backend.exception.BadRequestException;
import com.parksync.backend.exception.ConflictException;
import com.parksync.backend.exception.ResourceNotFoundException;
import com.parksync.backend.repository.VehicleRepository;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final AppUserService appUserService;

    public VehicleService(VehicleRepository vehicleRepository, AppUserService appUserService) {
        this.vehicleRepository = vehicleRepository;
        this.appUserService = appUserService;
    }

    public List<Vehicle> getVehiclesByUserId(Long userId) {
        appUserService.getRequiredUserById(userId);
        return vehicleRepository.findByUserId(userId);
    }

    public List<Vehicle> getActiveVehiclesByUserId(Long userId) {
        appUserService.getRequiredUserById(userId);
        return vehicleRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public Vehicle getRequiredVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));
    }

    @Transactional
    public Vehicle registerVehicle(VehicleRequest request) {
        AppUser user = appUserService.getRequiredUserById(request.getUserId());
        String normalizedNumber = request.getVehicleNumber().trim().toUpperCase();

        if (vehicleRepository.existsByVehicleNumber(normalizedNumber)) {
            throw new ConflictException("Vehicle with registration number '" + normalizedNumber + "' already exists");
        }

        validateVehicleType(request.getVehicleType());

        Vehicle vehicle = new Vehicle();
        vehicle.setUser(user);
        vehicle.setVehicleNumber(normalizedNumber);
        vehicle.setVehicleType(request.getVehicleType().trim().toUpperCase());
        vehicle.setVehicleModel(request.getVehicleModel() != null ? request.getVehicleModel().trim() : null);
        vehicle.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle updateVehicle(Long vehicleId, VehicleRequest request) {
        Vehicle vehicle = getRequiredVehicleById(vehicleId);
        String normalizedNumber = request.getVehicleNumber().trim().toUpperCase();

        if (!vehicle.getVehicleNumber().equalsIgnoreCase(normalizedNumber)) {
            if (vehicleRepository.existsByVehicleNumber(normalizedNumber)) {
                throw new ConflictException("Vehicle with registration number '" + normalizedNumber + "' already exists");
            }
            vehicle.setVehicleNumber(normalizedNumber);
        }

        if (request.getVehicleType() != null) {
            validateVehicleType(request.getVehicleType());
            vehicle.setVehicleType(request.getVehicleType().trim().toUpperCase());
        }

        if (request.getVehicleModel() != null) {
            vehicle.setVehicleModel(request.getVehicleModel().trim());
        }

        if (request.getIsActive() != null) {
            vehicle.setIsActive(request.getIsActive());
        }

        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle deactivateVehicle(Long vehicleId) {
        Vehicle vehicle = getRequiredVehicleById(vehicleId);
        vehicle.setIsActive(false);
        return vehicleRepository.save(vehicle);
    }

    private void validateVehicleType(String type) {
        if (type == null) {
            throw new BadRequestException("Vehicle type is required");
        }
        String t = type.trim().toUpperCase();
        if (!t.equals("CAR") && !t.equals("BIKE") && !t.equals("SUV") && !t.equals("TRUCK") && !t.equals("OTHER")) {
            throw new BadRequestException("Invalid vehicle type: " + type + ". Allowed types: CAR, BIKE, SUV, TRUCK, OTHER");
        }
    }
}
