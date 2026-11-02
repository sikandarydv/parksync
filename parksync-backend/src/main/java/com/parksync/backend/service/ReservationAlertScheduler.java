package com.parksync.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.parksync.backend.entity.Reservation;
import com.parksync.backend.repository.ReservationRepository;

@Component
public class ReservationAlertScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReservationAlertScheduler.class);

    private final ReservationRepository reservationRepository;
    private final EmailService emailService;

    public ReservationAlertScheduler(ReservationRepository reservationRepository, EmailService emailService) {
        this.reservationRepository = reservationRepository;
        this.emailService = emailService;
    }

    /**
     * Runs every 60 seconds.
     * Finds active CONFIRMED reservations whose end time is approaching (within the next 30 minutes)
     * and sends an alert email if an alert has not yet been dispatched.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndDispatchExpiryAlerts() {
        LocalDateTime now = LocalDateTime.now();
        // Look ahead 30 minutes for impending expirations
        LocalDateTime alertWindow = now.plusMinutes(30);

        List<Reservation> expiring = reservationRepository.findExpiringReservations(now.minusMinutes(5), alertWindow);

        if (!expiring.isEmpty()) {
            logger.info("Found {} active reservations approaching expiration", expiring.size());
            for (Reservation res : expiring) {
                try {
                    emailService.sendExpiryAlertEmail(res);
                    res.setAlertSent(true);
                    reservationRepository.save(res);
                    logger.info("Dispatched expiry alert for reservation #{} to {}", res.getReservationId(), 
                            res.getUser() != null ? res.getUser().getEmail() : "N/A");
                } catch (Exception e) {
                    logger.error("Error processing alert for reservation #{}: {}", res.getReservationId(), e.getMessage());
                }
            }
        }
    }
}
