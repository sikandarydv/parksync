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

import com.parksync.backend.dto.request.ParkingLotRequest;
import com.parksync.backend.entity.ParkingLot;
import com.parksync.backend.service.ParkingLotService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    public ParkingLotController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    @GetMapping
    public List<ParkingLot> getAllParkingLots(@RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        if (activeOnly) {
            return parkingLotService.getActiveParkingLots();
        }
        return parkingLotService.getAllParkingLots();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingLot> getParkingLotById(@PathVariable Long id) {
        return parkingLotService.getParkingLotById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ParkingLot> addParkingLot(@Valid @RequestBody ParkingLotRequest request) {
        ParkingLot created = parkingLotService.createParkingLot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingLot> updateParkingLot(
            @PathVariable Long id,
            @Valid @RequestBody ParkingLotRequest request) {
        ParkingLot updated = parkingLotService.updateParkingLot(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ParkingLot> deactivateParkingLot(@PathVariable Long id) {
        ParkingLot lot = parkingLotService.deactivateParkingLot(id);
        return ResponseEntity.ok(lot);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteParkingLot(@PathVariable Long id) {
        parkingLotService.deleteParkingLot(id);
        return ResponseEntity.ok(Map.of("message", "Parking lot deactivated successfully"));
    }
}
