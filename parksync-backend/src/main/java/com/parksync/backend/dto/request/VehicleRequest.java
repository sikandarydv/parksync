package com.parksync.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VehicleRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Vehicle number is required")
    @Size(max = 20, message = "Vehicle number cannot exceed 20 characters")
    private String vehicleNumber;

    @NotBlank(message = "Vehicle type is required")
    @Pattern(regexp = "CAR|BIKE|SUV|TRUCK|OTHER", message = "Vehicle type must be CAR, BIKE, SUV, TRUCK, or OTHER")
    private String vehicleType;

    @Size(max = 100, message = "Vehicle model cannot exceed 100 characters")
    private String vehicleModel;

    private Boolean isActive = true;

    public VehicleRequest() {
    }

    public VehicleRequest(Long userId, String vehicleNumber, String vehicleType, String vehicleModel) {
        this.userId = userId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.vehicleModel = vehicleModel;
        this.isActive = true;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
