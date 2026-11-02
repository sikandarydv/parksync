package com.parksync.backend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.parksync.backend.entity.Payment;
import com.parksync.backend.entity.Reservation;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:sikandar.ydv79995@gmail.com}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendExpiryAlertEmail(Reservation reservation) {
        if (reservation.getUser() == null || reservation.getUser().getEmail() == null) {
            logger.warn("Cannot send expiry email: User email is null for reservation #{}", reservation.getReservationId());
            return;
        }

        String toEmail = reservation.getUser().getEmail();
        String userName = reservation.getUser().getFullName() != null ? reservation.getUser().getFullName() : "Valued Driver";
        String lotName = reservation.getSlot() != null && reservation.getSlot().getParkingLot() != null 
                ? reservation.getSlot().getParkingLot().getLotName() : "Parking Facility";
        String bayName = reservation.getSlot() != null ? reservation.getSlot().getSlotNumber() : "Assigned Bay";
        String vehiclePlate = reservation.getVehicle() != null ? reservation.getVehicle().getVehicleNumber() : "";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy");
        String formattedEndTime = reservation.getEndTime() != null ? reservation.getEndTime().format(dtf) : "soon";

        String subject = "🔔 ParkSync Alert: Your Parking at " + bayName + " is Expiring Soon";

        String htmlContent = "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b;'>"
                + "<div style='max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 14px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>"
                + "  <div style='background: #064e3b; padding: 24px; text-align: center; color: #ffffff;'>"
                + "    <h1 style='margin: 0; font-size: 22px; font-weight: 800; letter-spacing: -0.5px;'>ParkSync Smart Parking</h1>"
                + "    <p style='margin: 6px 0 0 0; font-size: 13px; color: #6ee7b7;'>Active Parking Session Expiration Alert</p>"
                + "  </div>"
                + "  <div style='padding: 28px;'>"
                + "    <h2 style='margin-top: 0; font-size: 18px; color: #0f172a;'>Hello " + userName + ",</h2>"
                + "    <p style='font-size: 14px; line-height: 1.6; color: #475569;'>"
                + "      Your reserved parking session at <strong>" + lotName + "</strong> is scheduled to end at "
                + "      <span style='background: #fef3c7; color: #92400e; font-weight: 700; padding: 2px 6px; border-radius: 4px;'>" + formattedEndTime + "</span>."
                + "    </p>"
                + "    <div style='background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px; margin: 20px 0;'>"
                + "      <div style='display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px;'>"
                + "        <span style='color: #64748b;'>Parking Bay:</span>"
                + "        <strong style='color: #064e3b;'>Bay " + bayName + "</strong>"
                + "      </div>"
                + "      <div style='display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px;'>"
                + "        <span style='color: #64748b;'>Registered Vehicle:</span>"
                + "        <strong style='color: #0f172a;'>" + vehiclePlate + "</strong>"
                + "      </div>"
                + "      <div style='display: flex; justify-content: space-between; font-size: 13px;'>"
                + "        <span style='color: #64748b;'>Booking Reference:</span>"
                + "        <strong style='font-family: monospace;'>#" + reservation.getReservationId() + "</strong>"
                + "      </div>"
                + "    </div>"
                + "    <p style='font-size: 13px; color: #b45309; background: #fffbeb; padding: 10px 14px; border-radius: 8px; border-left: 4px solid #f59e0b; margin-bottom: 24px;'>"
                + "      <strong>Notice:</strong> An additional +1 hour grace buffer has been held for your vehicle. If you require more time, please extend your session now to avoid overstay penalties."
                + "    </p>"
                + "    <div style='text-align: center; margin: 28px 0;'>"
                + "      <a href='http://localhost:5173' target='_blank' style='background: #064e3b; color: #ffffff; text-decoration: none; padding: 14px 28px; border-radius: 8px; font-weight: 700; font-size: 14px; display: inline-block; box-shadow: 0 2px 6px rgba(6,78,59,0.3);'>"
                + "        Extend Parking Session Online &rarr;"
                + "      </a>"
                + "    </div>"
                + "    <p style='font-size: 12px; color: #94a3b8; text-align: center; margin: 0;'>"
                + "      This is an automated alert from ParkSync Smart Facility Management."
                + "    </p>"
                + "  </div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail, "ParkSync Alerts");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Successfully sent expiry alert email to {} for reservation #{}", toEmail, reservation.getReservationId());
        } catch (Exception ex) {
            logger.error("Failed to send expiry alert email to {}: {}", toEmail, ex.getMessage(), ex);
        }
    }

    @Async
    public void sendBookingConfirmationEmail(Reservation reservation, Payment payment) {
        if (reservation.getUser() == null || reservation.getUser().getEmail() == null) {
            logger.warn("Cannot send confirmation email: User email is null for reservation #{}", reservation.getReservationId());
            return;
        }

        String toEmail = reservation.getUser().getEmail();
        String userName = reservation.getUser().getFullName() != null ? reservation.getUser().getFullName() : "Valued Driver";
        String lotName = reservation.getSlot() != null && reservation.getSlot().getParkingLot() != null 
                ? reservation.getSlot().getParkingLot().getLotName() : "Parking Facility";
        String lotAddress = reservation.getSlot() != null && reservation.getSlot().getParkingLot() != null 
                ? reservation.getSlot().getParkingLot().getAddress() : "";
        String lotCity = reservation.getSlot() != null && reservation.getSlot().getParkingLot() != null 
                ? reservation.getSlot().getParkingLot().getCity() : "";
        String fullLocation = lotAddress + (lotCity != null && !lotCity.isBlank() ? ", " + lotCity : "");
        String bayName = reservation.getSlot() != null ? reservation.getSlot().getSlotNumber() : "Assigned Bay";
        String slotType = reservation.getSlot() != null ? reservation.getSlot().getSlotType() : "REGULAR";
        String vehiclePlate = reservation.getVehicle() != null ? reservation.getVehicle().getVehicleNumber() : "";
        String vehicleModel = reservation.getVehicle() != null && reservation.getVehicle().getVehicleModel() != null 
                ? reservation.getVehicle().getVehicleModel() : "";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy");
        String formattedStart = reservation.getStartTime() != null ? reservation.getStartTime().format(dtf) : "N/A";
        String formattedEnd = reservation.getEndTime() != null ? reservation.getEndTime().format(dtf) : "N/A";

        String txnRef = payment != null && payment.getTransactionReference() != null ? payment.getTransactionReference() : ("TXN-BK" + reservation.getReservationId());
        String payMethod = payment != null && payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "CONFIRMED";

        String mapsQuery = URLEncoder.encode(lotName + " " + fullLocation, StandardCharsets.UTF_8);
        String mapsLink = "https://www.google.com/maps/search/?api=1&query=" + mapsQuery;

        String subject = "✅ Booking Confirmed: Bay " + bayName + " at " + lotName + " | ParkSync";

        String htmlContent = "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b;'>"
                + "<div style='max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 14px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>"
                + "  <div style='background: #064e3b; padding: 28px 24px; text-align: center; color: #ffffff;'>"
                + "    <div style='display: inline-block; background: #10b981; color: #ffffff; border-radius: 50%; width: 44px; height: 44px; line-height: 44px; font-size: 24px; margin-bottom: 12px;'>✓</div>"
                + "    <h1 style='margin: 0; font-size: 22px; font-weight: 800; letter-spacing: -0.5px;'>Parking Space Confirmed!</h1>"
                + "    <p style='margin: 6px 0 0 0; font-size: 13px; color: #6ee7b7;'>Reservation ID: #" + reservation.getReservationId() + " • " + txnRef + "</p>"
                + "  </div>"
                + "  <div style='padding: 28px;'>"
                + "    <h2 style='margin-top: 0; font-size: 17px; color: #0f172a;'>Hi " + userName + ",</h2>"
                + "    <p style='font-size: 14px; line-height: 1.6; color: #475569; margin-bottom: 20px;'>"
                + "      Your parking slot has been successfully reserved and secured. Please display your registration plate at the facility barrier."
                + "    </p>"
                + "    <div style='background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 18px; margin-bottom: 20px;'>"
                + "      <div style='display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px;'>"
                + "        <span style='color: #64748b;'>Parking Facility:</span>"
                + "        <strong style='color: #0f172a; text-align: right;'>" + lotName + "</strong>"
                + "      </div>"
                + "      <div style='display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px;'>"
                + "        <span style='color: #64748b;'>Facility Location:</span>"
                + "        <span style='color: #334155; text-align: right; font-weight: 600;'>" + fullLocation + "</span>"
                + "      </div>"
                + "      <div style='display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px;'>"
                + "        <span style='color: #64748b;'>Assigned Bay:</span>"
                + "        <strong style='color: #064e3b; font-size: 15px;'>Bay " + bayName + " (" + slotType + ")</strong>"
                + "      </div>"
                + "      <div style='display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px;'>"
                + "        <span style='color: #64748b;'>Registered Vehicle:</span>"
                + "        <strong style='color: #0f172a;'>" + vehiclePlate + (vehicleModel.isBlank() ? "" : " • " + vehicleModel) + "</strong>"
                + "      </div>"
                + "      <div style='display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px; border-top: 1px solid #e2e8f0; padding-top: 10px;'>"
                + "        <span style='color: #64748b;'>Start Schedule:</span>"
                + "        <strong style='color: #0f172a;'>" + formattedStart + "</strong>"
                + "      </div>"
                + "      <div style='display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px;'>"
                + "        <span style='color: #64748b;'>End Schedule:</span>"
                + "        <strong style='color: #0f172a;'>" + formattedEnd + "</strong>"
                + "      </div>"
                + "      <div style='display: flex; justify-content: space-between; font-size: 14px; border-top: 1px solid #e2e8f0; padding-top: 10px;'>"
                + "        <strong style='color: #0f172a;'>Total Paid (" + payMethod + "):</strong>"
                + "        <strong style='color: #10b981; font-size: 16px;'>₹" + reservation.getTotalAmount() + "</strong>"
                + "      </div>"
                + "    </div>"
                + "    <div style='text-align: center; margin: 24px 0; display: flex; gap: 12px; justify-content: center;'>"
                + "      <a href='" + mapsLink + "' target='_blank' style='background: #0f172a; color: #ffffff; text-decoration: none; padding: 12px 22px; border-radius: 8px; font-weight: 700; font-size: 13px; display: inline-block;'>"
                + "        📍 Open in Google Maps"
                + "      </a>"
                + "      <a href='http://localhost:5173' target='_blank' style='background: #064e3b; color: #ffffff; text-decoration: none; padding: 12px 22px; border-radius: 8px; font-weight: 700; font-size: 13px; display: inline-block;'>"
                + "        View in ParkSync App"
                + "      </a>"
                + "    </div>"
                + "    <p style='font-size: 12px; color: #64748b; background: #f0fdf4; border: 1px solid #bbf7d0; padding: 10px 14px; border-radius: 8px; margin-bottom: 20px;'>"
                + "      🛡️ <strong>Buffer Protection Included:</strong> Your parking space is secured with an automatic +1 hour grace buffer against incoming reservations."
                + "    </p>"
                + "    <p style='font-size: 12px; color: #94a3b8; text-align: center; margin: 0;'>"
                + "      Thank you for choosing ParkSync Smart Facility Management."
                + "    </p>"
                + "  </div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail, "ParkSync Reservations");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Successfully sent booking confirmation email to {} for reservation #{}", toEmail, reservation.getReservationId());
        } catch (Exception ex) {
            logger.error("Failed to send booking confirmation email to {}: {}", toEmail, ex.getMessage(), ex);
        }
    }
}
