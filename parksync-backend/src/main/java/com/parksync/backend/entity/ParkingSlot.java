package com.parksync.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "parking_slots", uniqueConstraints = {
    @UniqueConstraint(name = "unique_slot_per_lot", columnNames = {"lot_id", "slot_number"})
})
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Long slotId;

    @Column(name = "lot_id", insertable = false, updatable = false)
    private Long lotId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lot_id", nullable = false)
    @JsonIgnore
    private ParkingLot parkingLot;

    @Column(name = "slot_number", nullable = false)
    private String slotNumber;

    @Column(name = "slot_type")
    private String slotType = "REGULAR";

    @Column(nullable = false)
    private String status = "AVAILABLE";

    @Column(name = "is_active")
    private Boolean isActive = true;

    public ParkingSlot() {
    }

    public ParkingSlot(ParkingLot parkingLot, String slotNumber, String slotType, String status) {
        this.parkingLot = parkingLot;
        this.slotNumber = slotNumber;
        this.slotType = slotType != null ? slotType : "REGULAR";
        this.status = status != null ? status : "AVAILABLE";
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        if (this.slotType == null || this.slotType.isBlank()) {
            this.slotType = "REGULAR";
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "AVAILABLE";
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Long getLotId() {
        if (lotId != null) {
            return lotId;
        }
        return parkingLot != null ? parkingLot.getLotId() : null;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public void setParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
        if (parkingLot != null) {
            this.lotId = parkingLot.getLotId();
        }
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getSlotType() {
        return slotType;
    }

    public void setSlotType(String slotType) {
        this.slotType = slotType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
