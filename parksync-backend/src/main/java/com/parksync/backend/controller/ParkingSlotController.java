package com.parksync.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parksync.backend.dto.request.ParkingSlotRequest;
import com.parksync.backend.entity.ParkingSlot;
import com.parksync.backend.service.ParkingSlotService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/slots")
public class ParkingSlotController {

    private final ParkingSlotService parkingSlotService;

    public ParkingSlotController(ParkingSlotService parkingSlotService) {
        this.parkingSlotService = parkingSlotService;
    }

    @PostMapping
    public ResponseEntity<ParkingSlot> addSlot(@Valid @RequestBody ParkingSlotRequest request) {
        ParkingSlot created = parkingSlotService.addSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/lot/{lotId}")
    public List<ParkingSlot> getSlotsByLot(
            @PathVariable Long lotId,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        if (activeOnly) {
            return parkingSlotService.getActiveSlotsByLotId(lotId);
        }
        return parkingSlotService.getSlotsByLotId(lotId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingSlot> getSlotById(@PathVariable Long id) {
        ParkingSlot slot = parkingSlotService.getRequiredSlotById(id);
        return ResponseEntity.ok(slot);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingSlot> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody ParkingSlotRequest request) {
        ParkingSlot updated = parkingSlotService.updateSlot(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ParkingSlot> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        ParkingSlot updated = parkingSlotService.updateSlotStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ParkingSlot> deactivateSlot(@PathVariable Long id) {
        ParkingSlot updated = parkingSlotService.deactivateSlot(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSlot(@PathVariable Long id) {
        parkingSlotService.deactivateSlot(id);
        return ResponseEntity.ok(Map.of("message", "Parking slot deactivated successfully"));
    }
}
