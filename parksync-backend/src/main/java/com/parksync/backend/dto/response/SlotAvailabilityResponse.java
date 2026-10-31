package com.parksync.backend.dto.response;

public class SlotAvailabilityResponse {

    private Long slotId;
    private String slotNumber;
    private String slotType;
    private boolean available;
    private String status;

    public SlotAvailabilityResponse() {
    }

    public SlotAvailabilityResponse(Long slotId, String slotNumber, String slotType, boolean available, String status) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.slotType = slotType;
        this.available = available;
        this.status = status;
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

    public String getSlotType() {
        return slotType;
    }

    public void setSlotType(String slotType) {
        this.slotType = slotType;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
