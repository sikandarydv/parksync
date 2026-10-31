package com.parksync.backend;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.parksync.backend.dto.request.LoginRequest;
import com.parksync.backend.dto.request.ParkingLotRequest;
import com.parksync.backend.dto.request.ParkingSlotRequest;
import com.parksync.backend.dto.request.PaymentRequest;
import com.parksync.backend.dto.request.RegisterRequest;
import com.parksync.backend.dto.request.ReservationRequest;
import com.parksync.backend.dto.request.VehicleRequest;
import com.parksync.backend.dto.response.AuthResponse;
import com.parksync.backend.dto.response.PaymentResponse;
import com.parksync.backend.dto.response.ReservationResponse;
import com.parksync.backend.entity.ParkingLot;
import com.parksync.backend.entity.ParkingSlot;
import com.parksync.backend.entity.Vehicle;
import com.parksync.backend.exception.BadRequestException;
import com.parksync.backend.exception.ConflictException;
import com.parksync.backend.service.AppUserService;
import com.parksync.backend.service.ParkingLotService;
import com.parksync.backend.service.ParkingSlotService;
import com.parksync.backend.service.PaymentService;
import com.parksync.backend.service.ReservationService;
import com.parksync.backend.service.VehicleService;

@SpringBootTest
public class ParkSyncEndToEndTests {

    @Autowired
    private ParkingLotService parkingLotService;

    @Autowired
    private ParkingSlotService parkingSlotService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private PaymentService paymentService;

    @Test
    void testPhaseA_ParkingLotCrudAndActiveHandling() {
        ParkingLotRequest request = new ParkingLotRequest(
                "Test North Garage " + System.currentTimeMillis(),
                "123 University Road",
                "Chennai",
                50,
                new BigDecimal("40.00"),
                true
        );

        ParkingLot created = parkingLotService.createParkingLot(request);
        assertNotNull(created.getLotId());
        assertNotNull(created.getCreatedAt(), "Database/JPA createdAt should be populated");
        assertEquals("Chennai", created.getCity());

        // Update
        request.setPricePerHour(new BigDecimal("45.00"));
        ParkingLot updated = parkingLotService.updateParkingLot(created.getLotId(), request);
        assertEquals(new BigDecimal("45.00"), updated.getPricePerHour());

        // Deactivate
        ParkingLot deactivated = parkingLotService.deactivateParkingLot(created.getLotId());
        assertFalse(deactivated.getIsActive());
    }

    @Test
    void testPhaseB_SlotManagementAndDuplicatePrevention() {
        ParkingLot lot = parkingLotService.getRequiredParkingLotById(1L);

        String uniqueSlotNum = "TEST-" + (System.currentTimeMillis() % 100000);
        ParkingSlotRequest slotReq = new ParkingSlotRequest(lot.getLotId(), uniqueSlotNum, "REGULAR", "AVAILABLE");
        ParkingSlot created = parkingSlotService.addSlot(slotReq);

        assertNotNull(created.getSlotId());
        assertEquals(uniqueSlotNum, created.getSlotNumber());

        // Test duplicate slot number conflict
        assertThrows(ConflictException.class, () -> {
            parkingSlotService.addSlot(slotReq);
        }, "Should prevent duplicate slot numbers in same parking lot");
    }

    @Test
    void testPhaseC_UserRegistrationLoginAndBCrypt() {
        String email = "testuser" + System.currentTimeMillis() + "@parksync.com";
        RegisterRequest regReq = new RegisterRequest("Test Student", email, null, "SecurePass123", "ROLE_USER");
        AuthResponse authRes = appUserService.register(regReq);

        assertNotNull(authRes.getToken());
        assertNotNull(authRes.getUser());
        assertEquals(email, authRes.getUser().getEmail());

        // Duplicate registration check
        assertThrows(ConflictException.class, () -> {
            appUserService.register(regReq);
        }, "Should prevent duplicate email registration");

        // Login check
        LoginRequest loginReq = new LoginRequest(email, "SecurePass123");
        AuthResponse loginRes = appUserService.login(loginReq);
        assertNotNull(loginRes.getToken());
    }

    @Test
    void testPhaseD_VehicleManagement() {
        String email = "vehicletest" + System.currentTimeMillis() + "@parksync.com";
        AuthResponse userAuth = appUserService.register(new RegisterRequest("Driver Dave", email, null, "Pass1234", "ROLE_USER"));
        Long userId = userAuth.getUser().getUserId();

        String vehNum = "TN-01-AB-" + (System.currentTimeMillis() % 10000);
        VehicleRequest vReq = new VehicleRequest(userId, vehNum, "CAR", "Honda City");
        Vehicle created = vehicleService.registerVehicle(vReq);

        assertNotNull(created.getVehicleId());
        assertEquals(vehNum, created.getVehicleNumber());

        // Duplicate vehicle number check
        assertThrows(ConflictException.class, () -> {
            vehicleService.registerVehicle(vReq);
        }, "Should prevent duplicate vehicle numbers");
    }

    @Test
    void testPhaseE_ReservationPriceCalculationAndDoubleBookingPrevention() {
        // Setup user and vehicle
        String email = "reservetest" + System.currentTimeMillis() + "@parksync.com";
        AuthResponse userAuth = appUserService.register(new RegisterRequest("Booker Bob", email, null, "Pass1234", "ROLE_USER"));
        Long userId = userAuth.getUser().getUserId();

        String vehNum = "KA-03-CD-" + (System.currentTimeMillis() % 10000);
        Vehicle vehicle = vehicleService.registerVehicle(new VehicleRequest(userId, vehNum, "CAR", "Hyundai i20"));

        // Setup a dedicated slot for test
        ParkingLot lot = parkingLotService.getRequiredParkingLotById(1L);
        String slotNum = "RES-" + (System.currentTimeMillis() % 100000);
        ParkingSlot slot = parkingSlotService.addSlot(new ParkingSlotRequest(lot.getLotId(), slotNum, "REGULAR", "AVAILABLE"));

        LocalDateTime t1 = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime t2 = t1.plusHours(3); // 3 hours

        // 1. Create first reservation
        ReservationRequest rReq1 = new ReservationRequest(userId, vehicle.getVehicleId(), slot.getSlotId(), t1, t2);
        ReservationResponse res1 = reservationService.createReservation(rReq1);

        assertNotNull(res1.getReservationId());
        assertEquals("PENDING", res1.getStatus());
        // Rate is 30.00/hr, 3 hours -> 90.00
        assertEquals(new BigDecimal("90.00"), res1.getTotalAmount());

        // 2. Attempt overlapping reservation (11:00 to 12:00, completely within t1..t2)
        LocalDateTime overlapStart = t1.plusHours(1);
        LocalDateTime overlapEnd = t1.plusHours(2);
        ReservationRequest rReqOverlap = new ReservationRequest(userId, vehicle.getVehicleId(), slot.getSlotId(), overlapStart, overlapEnd);

        assertThrows(ConflictException.class, () -> {
            reservationService.createReservation(rReqOverlap);
        }, "Double booking must be prevented by overlap check");

        // 3. Attempt invalid time (end before start)
        ReservationRequest rReqInvalidTime = new ReservationRequest(userId, vehicle.getVehicleId(), slot.getSlotId(), t2, t1);
        assertThrows(BadRequestException.class, () -> {
            reservationService.createReservation(rReqInvalidTime);
        }, "End time before start time must be rejected");

        // 4. Test non-overlapping reservation after t2 (should succeed)
        LocalDateTime t3 = t2.plusHours(1);
        LocalDateTime t4 = t3.plusHours(2);
        ReservationRequest rReqAfter = new ReservationRequest(userId, vehicle.getVehicleId(), slot.getSlotId(), t3, t4);
        ReservationResponse resAfter = reservationService.createReservation(rReqAfter);
        assertNotNull(resAfter.getReservationId());
    }

    @Test
    void testPhaseF_SimulatedPaymentFlow() {
        // Setup user, vehicle, reservation
        String email = "paytest" + System.currentTimeMillis() + "@parksync.com";
        AuthResponse userAuth = appUserService.register(new RegisterRequest("Payer Paul", email, null, "Pass1234", "ROLE_USER"));
        Long userId = userAuth.getUser().getUserId();

        String vehNum = "DL-01-XY-" + (System.currentTimeMillis() % 10000);
        Vehicle vehicle = vehicleService.registerVehicle(new VehicleRequest(userId, vehNum, "SUV", "Tata Harrier"));

        ParkingLot lot = parkingLotService.getRequiredParkingLotById(1L);
        String slotNum = "PAY-" + (System.currentTimeMillis() % 100000);
        ParkingSlot slot = parkingSlotService.addSlot(new ParkingSlotRequest(lot.getLotId(), slotNum, "REGULAR", "AVAILABLE"));

        LocalDateTime start = LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        ReservationResponse reservation = reservationService.createReservation(
                new ReservationRequest(userId, vehicle.getVehicleId(), slot.getSlotId(), start, end)
        );

        // Process simulated payment
        PaymentRequest payReq = new PaymentRequest(reservation.getReservationId(), "UPI");
        PaymentResponse payRes = paymentService.processPayment(payReq);

        assertNotNull(payRes.getPaymentId());
        assertEquals("SUCCESS", payRes.getPaymentStatus());
        assertTrue(payRes.getTransactionReference().startsWith("TXN-"));

        // Verify reservation status transitioned to CONFIRMED
        ReservationResponse updatedRes = reservationService.getReservationDetails(reservation.getReservationId());
        assertEquals("CONFIRMED", updatedRes.getStatus());
    }
}
