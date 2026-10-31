package com.parksync.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parksync.backend.dto.request.ParkingLotRequest;
import com.parksync.backend.entity.ParkingLot;
import com.parksync.backend.exception.ResourceNotFoundException;
import com.parksync.backend.repository.ParkingLotRepository;

@Service
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;

    public ParkingLotService(ParkingLotRepository parkingLotRepository) {
        this.parkingLotRepository = parkingLotRepository;
    }

    public List<ParkingLot> getAllParkingLots() {
        return parkingLotRepository.findAll();
    }

    public List<ParkingLot> getActiveParkingLots() {
        return parkingLotRepository.findByIsActiveTrue();
    }

    public Optional<ParkingLot> getParkingLotById(Long id) {
        return parkingLotRepository.findById(id);
    }

    public ParkingLot getRequiredParkingLotById(Long id) {
        return parkingLotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found with ID: " + id));
    }

    @Transactional
    public ParkingLot saveParkingLot(ParkingLot parkingLot) {
        return parkingLotRepository.save(parkingLot);
    }

    @Transactional
    public ParkingLot createParkingLot(ParkingLotRequest request) {
        ParkingLot lot = new ParkingLot();
        lot.setLotName(request.getLotName());
        lot.setAddress(request.getAddress());
        lot.setCity(request.getCity());
        lot.setTotalSlots(request.getTotalSlots());
        lot.setPricePerHour(request.getPricePerHour());
        lot.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return parkingLotRepository.save(lot);
    }

    @Transactional
    public ParkingLot updateParkingLot(Long id, ParkingLotRequest request) {
        ParkingLot existingLot = getRequiredParkingLotById(id);

        existingLot.setLotName(request.getLotName());
        existingLot.setAddress(request.getAddress());
        existingLot.setCity(request.getCity());
        existingLot.setTotalSlots(request.getTotalSlots());
        existingLot.setPricePerHour(request.getPricePerHour());
        if (request.getIsActive() != null) {
            existingLot.setIsActive(request.getIsActive());
        }

        return parkingLotRepository.save(existingLot);
    }

    @Transactional
    public ParkingLot deactivateParkingLot(Long id) {
        ParkingLot lot = getRequiredParkingLotById(id);
        lot.setIsActive(false);
        return parkingLotRepository.save(lot);
    }

    @Transactional
    public void deleteParkingLot(Long id) {
        // Soft delete / deactivation by default to preserve historical integrity
        deactivateParkingLot(id);
    }
}
