package com.parksync.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.parksync.backend.entity.Reservation;

public class ReservationResponse {

    private Long reservationId;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long vehicleId;
    private String vehicleNumber;
    private String vehicleType;
    private String vehicleModel;
    private Long slotId;
    private String slotNumber;
    private Long lotId;
    private String lotName;
    private String lotAddress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    public ReservationResponse() {
    }

    public static ReservationResponse fromEntity(Reservation r) {
        if (r == null) return null;
        ReservationResponse dto = new ReservationResponse();
        dto.setReservationId(r.getReservationId());
        
        if (r.getUser() != null) {
            dto.setUserId(r.getUser().getUserId());
            dto.setUserName(r.getUser().getFullName());
            dto.setUserEmail(r.getUser().getEmail());
        }

        if (r.getVehicle() != null) {
            dto.setVehicleId(r.getVehicle().getVehicleId());
            dto.setVehicleNumber(r.getVehicle().getVehicleNumber());
            dto.setVehicleType(r.getVehicle().getVehicleType());
            dto.setVehicleModel(r.getVehicle().getVehicleModel());
        }

        if (r.getSlot() != null) {
            dto.setSlotId(r.getSlot().getSlotId());
            dto.setSlotNumber(r.getSlot().getSlotNumber());
            if (r.getSlot().getParkingLot() != null) {
                dto.setLotId(r.getSlot().getParkingLot().getLotId());
                dto.setLotName(r.getSlot().getParkingLot().getLotName());
                dto.setLotAddress(r.getSlot().getParkingLot().getAddress());
            }
        }

        dto.setStartTime(r.getStartTime());
        dto.setEndTime(r.getEndTime());
        dto.setStatus(r.getStatus());
        dto.setTotalAmount(r.getTotalAmount());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
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

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public String getLotName() {
        return lotName;
    }

    public void setLotName(String lotName) {
        this.lotName = lotName;
    }

    public String getLotAddress() {
        return lotAddress;
    }

    public void setLotAddress(String lotAddress) {
        this.lotAddress = lotAddress;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
