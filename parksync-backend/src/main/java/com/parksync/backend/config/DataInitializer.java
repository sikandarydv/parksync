package com.parksync.backend.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.parksync.backend.entity.AppUser;
import com.parksync.backend.entity.ParkingLot;
import com.parksync.backend.entity.ParkingSlot;
import com.parksync.backend.repository.AppUserRepository;
import com.parksync.backend.repository.ParkingLotRepository;
import com.parksync.backend.repository.ParkingSlotRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserRepository appUserRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            AppUserRepository appUserRepository,
            ParkingLotRepository parkingLotRepository,
            ParkingSlotRepository parkingSlotRepository,
            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed default Admin user if not present
        if (!appUserRepository.existsByEmail("admin@parksync.com")) {
            AppUser admin = new AppUser();
            admin.setFullName("System Administrator");
            admin.setEmail("admin@parksync.com");
            admin.setPhone("9876543210");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setRole("ROLE_ADMIN");
            admin.setIsActive(true);
            appUserRepository.save(admin);
            logger.info("Initialized default administrator account: admin@parksync.com / Admin@123");
        }

        // Seed demo regular user if not present
        if (!appUserRepository.existsByEmail("user@parksync.com")) {
            AppUser demoUser = new AppUser();
            demoUser.setFullName("Demo User");
            demoUser.setEmail("user@parksync.com");
            demoUser.setPhone("9876500000");
            demoUser.setPasswordHash(passwordEncoder.encode("User@123"));
            demoUser.setRole("ROLE_USER");
            demoUser.setIsActive(true);
            appUserRepository.save(demoUser);
            logger.info("Initialized demo user account: user@parksync.com / User@123");
        }

        // Seed initial slots for VIT Main Parking if it has no slots
        Optional<ParkingLot> vitLotOpt = parkingLotRepository.findById(1L);
        if (vitLotOpt.isPresent()) {
            ParkingLot vitLot = vitLotOpt.get();
            long slotCount = parkingSlotRepository.countByLotIdAndIsActiveTrue(vitLot.getLotId());
            if (slotCount == 0) {
                // Add regular slots
                for (int i = 1; i <= 6; i++) {
                    ParkingSlot slot = new ParkingSlot(vitLot, "A-" + i, "REGULAR", "AVAILABLE");
                    parkingSlotRepository.save(slot);
                }
                // Add VIP slots
                for (int i = 1; i <= 3; i++) {
                    ParkingSlot slot = new ParkingSlot(vitLot, "VIP-" + i, "VIP", "AVAILABLE");
                    parkingSlotRepository.save(slot);
                }
                // Add EV slots
                for (int i = 1; i <= 3; i++) {
                    ParkingSlot slot = new ParkingSlot(vitLot, "EV-" + i, "EV", "AVAILABLE");
                    parkingSlotRepository.save(slot);
                }
                logger.info("Initialized 12 sample parking slots for parking lot: {}", vitLot.getLotName());
            }
        }
    }
}
