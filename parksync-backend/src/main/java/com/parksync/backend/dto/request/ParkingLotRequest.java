package com.parksync.backend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ParkingLotRequest {

    @NotBlank(message = "Parking lot name is required")
    @Size(max = 100, message = "Lot name must be within 100 characters")
    private String lotName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be within 100 characters")
    private String city;

    @NotNull(message = "Total slots is required")
    @Min(value = 1, message = "Total slots must be at least 1")
    private Integer totalSlots;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price per hour must be non-negative")
    private BigDecimal pricePerHour;

    private Boolean isActive = true;

    public ParkingLotRequest() {
    }

    public ParkingLotRequest(String lotName, String address, String city, Integer totalSlots, BigDecimal pricePerHour, Boolean isActive) {
        this.lotName = lotName;
        this.address = address;
        this.city = city;
        this.totalSlots = totalSlots;
        this.pricePerHour = pricePerHour;
        this.isActive = isActive != null ? isActive : true;
    }

    public String getLotName() {
        return lotName;
    }

    public void setLotName(String lotName) {
        this.lotName = lotName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(Integer totalSlots) {
        this.totalSlots = totalSlots;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
