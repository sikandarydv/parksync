package com.parksync.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ParkingSlotRequest {

    @NotNull(message = "Parking lot ID is required")
    private Long lotId;

    @NotBlank(message = "Slot number is required")
    @Size(max = 20, message = "Slot number must not exceed 20 characters")
    private String slotNumber;

    private String slotType = "REGULAR";

    @Pattern(regexp = "AVAILABLE|OCCUPIED|RESERVED|MAINTENANCE", message = "Status must be AVAILABLE, OCCUPIED, RESERVED, or MAINTENANCE")
    private String status = "AVAILABLE";

    private Boolean isActive = true;

    public ParkingSlotRequest() {
    }

    public ParkingSlotRequest(Long lotId, String slotNumber, String slotType, String status) {
        this.lotId = lotId;
        this.slotNumber = slotNumber;
        this.slotType = slotType != null ? slotType : "REGULAR";
        this.status = status != null ? status : "AVAILABLE";
        this.isActive = true;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
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
