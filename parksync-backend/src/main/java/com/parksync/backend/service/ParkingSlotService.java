package com.parksync.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parksync.backend.dto.request.ParkingSlotRequest;
import com.parksync.backend.entity.ParkingLot;
import com.parksync.backend.entity.ParkingSlot;
import com.parksync.backend.exception.BadRequestException;
import com.parksync.backend.exception.ConflictException;
import com.parksync.backend.exception.ResourceNotFoundException;
import com.parksync.backend.repository.ParkingSlotRepository;

@Service
public class ParkingSlotService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingLotService parkingLotService;

    public ParkingSlotService(ParkingSlotRepository parkingSlotRepository, ParkingLotService parkingLotService) {
        this.parkingSlotRepository = parkingSlotRepository;
        this.parkingLotService = parkingLotService;
    }

    public List<ParkingSlot> getSlotsByLotId(Long lotId) {
        // verify lot exists
        parkingLotService.getRequiredParkingLotById(lotId);
        return parkingSlotRepository.findByLotId(lotId);
    }

    public List<ParkingSlot> getActiveSlotsByLotId(Long lotId) {
        parkingLotService.getRequiredParkingLotById(lotId);
        return parkingSlotRepository.findByLotIdAndIsActiveTrue(lotId);
    }

    public ParkingSlot getRequiredSlotById(Long slotId) {
        return parkingSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot not found with ID: " + slotId));
    }

    @Transactional
    public ParkingSlot addSlot(ParkingSlotRequest request) {
        ParkingLot lot = parkingLotService.getRequiredParkingLotById(request.getLotId());

        if (parkingSlotRepository.existsByLotIdAndSlotNumber(lot.getLotId(), request.getSlotNumber().trim())) {
            throw new ConflictException("Slot number '" + request.getSlotNumber() + "' already exists in parking lot: " + lot.getLotName());
        }

        ParkingSlot slot = new ParkingSlot();
        slot.setParkingLot(lot);
        slot.setSlotNumber(request.getSlotNumber().trim().toUpperCase());
        slot.setSlotType(request.getSlotType() != null ? request.getSlotType() : "REGULAR");
        slot.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");
        slot.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return parkingSlotRepository.save(slot);
    }

    @Transactional
    public ParkingSlot updateSlot(Long slotId, ParkingSlotRequest request) {
        ParkingSlot slot = getRequiredSlotById(slotId);

        String newSlotNumber = request.getSlotNumber().trim().toUpperCase();
        if (!slot.getSlotNumber().equalsIgnoreCase(newSlotNumber)) {
            if (parkingSlotRepository.existsByLotIdAndSlotNumber(slot.getLotId(), newSlotNumber)) {
                throw new ConflictException("Slot number '" + newSlotNumber + "' already exists in this lot");
            }
            slot.setSlotNumber(newSlotNumber);
        }

        if (request.getSlotType() != null) {
            slot.setSlotType(request.getSlotType());
        }
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            slot.setStatus(request.getStatus());
        }
        if (request.getIsActive() != null) {
            slot.setIsActive(request.getIsActive());
        }

        return parkingSlotRepository.save(slot);
    }

    @Transactional
    public ParkingSlot updateSlotStatus(Long slotId, String status) {
        ParkingSlot slot = getRequiredSlotById(slotId);
        validateStatus(status);
        slot.setStatus(status.toUpperCase());
        return parkingSlotRepository.save(slot);
    }

    @Transactional
    public ParkingSlot deactivateSlot(Long slotId) {
        ParkingSlot slot = getRequiredSlotById(slotId);
        slot.setIsActive(false);
        return parkingSlotRepository.save(slot);
    }

    private void validateStatus(String status) {
        if (status == null) {
            throw new BadRequestException("Slot status cannot be null");
        }
        String s = status.toUpperCase();
        if (!s.equals("AVAILABLE") && !s.equals("OCCUPIED") && !s.equals("RESERVED") && !s.equals("MAINTENANCE")) {
            throw new BadRequestException("Invalid slot status: " + status + ". Allowed: AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE");
        }
    }
}
