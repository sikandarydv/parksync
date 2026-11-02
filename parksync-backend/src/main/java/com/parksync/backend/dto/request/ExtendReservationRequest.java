package com.parksync.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ExtendReservationRequest {

    @NotNull(message = "Extra duration in hours is required")
    @Min(value = 1, message = "Minimum extension is 1 hour")
    @Max(value = 6, message = "Maximum extension is 6 hours")
    private Integer extraHours;

    public ExtendReservationRequest() {
    }

    public ExtendReservationRequest(Integer extraHours) {
        this.extraHours = extraHours;
    }

    public Integer getExtraHours() {
        return extraHours;
    }

    public void setExtraHours(Integer extraHours) {
        this.extraHours = extraHours;
    }
}
