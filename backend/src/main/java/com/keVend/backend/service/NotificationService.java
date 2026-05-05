package com.keVend.backend.service;

import com.keVend.backend.i18n.I18n;
import com.keVend.backend.model.Notification;
import com.keVend.backend.model.Reservation;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.NotificationRepository;
import com.keVend.backend.repository.ReservationRepository;
import com.keVend.backend.sms.SmsDeliveryException;
import com.keVend.backend.sms.SmsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Owns delivery of in-app/SMS notifications. Today this records each
 * notification in the DB and logs it; the actual push/SMS dispatch is left to
 * a dedicated transport adapter so the rest of the stack stays infra-agnostic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    /** FR-07: warning window before reservation expiry. */
    private static final int EXPIRY_WARNING_MINUTES = 10;
    /** FR-08: how often to re-nag an unpaid driver. */
    private static final int UNPAID_REMINDER_HOURS = 1;

    private final NotificationRepository notificationRepository;
    private final ReservationRepository reservationRepository;
    private final SmsGateway smsGateway;
    private final I18n i18n;

    /** Render a notification message bundle key in the user's preferred language. */
    public String renderFor(User user, String key, Object... args) {
        return i18n.tFor(user.getPreferredLocale(), key, args);
    }

    @Transactional
    public Notification record(User user, Reservation reservation,
                               Notification.NotificationType type,
                               Notification.NotificationChannel channel,
                               String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setReservation(reservation);
        n.setType(type);
        n.setChannel(channel);
        n.setMessage(message);
        n.setSentAt(LocalDateTime.now());

        // SMS goes via the gateway; PUSH is recorded for the in-app inbox to read.
        // Failed SMS is persisted as FAILED so retries / inspection are possible.
        if (channel == Notification.NotificationChannel.SMS) {
            if (user.getPhone() == null || user.getPhone().isBlank()) {
                n.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
            } else {
                try {
                    smsGateway.send(user.getPhone(), message);
                    n.setDeliveryStatus(Notification.DeliveryStatus.DELIVERED);
                } catch (SmsDeliveryException ex) {
                    log.warn("[notification] SMS failed user={} type={}: {}",
                            user.getId(), type, ex.getMessage());
                    n.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
                }
            }
        } else {
            n.setDeliveryStatus(Notification.DeliveryStatus.DELIVERED);
        }

        Notification saved = notificationRepository.save(n);
        log.info("[notification] type={} channel={} status={} user={} reservation={}",
                type, channel, saved.getDeliveryStatus(),
                user.getId(), reservation != null ? reservation.getId() : null);
        return saved;
    }

    public List<Notification> inboxFor(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * FR-07: every minute, dispatch both expiry-related notifications:
     *  - first warning 10 minutes before endTime (sent once)
     *  - second notification at the moment endTime is reached (sent once)
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void dispatchExpiryNotifications() {
        Instant now = Instant.now();
        Instant warningUntil = now.plus(EXPIRY_WARNING_MINUTES, ChronoUnit.MINUTES);

        // FR-07 part 1: 10-minute warning
        for (Reservation r : reservationRepository.findUpcomingExpiries(now, warningUntil)) {
            String msg = renderFor(r.getDriver(),
                    "notification.expiry.warning",
                    r.getParking().getName(), EXPIRY_WARNING_MINUTES);
            record(r.getDriver(), r, Notification.NotificationType.EXPIRY_WARNING,
                    Notification.NotificationChannel.PUSH, msg);
            r.setExpiryWarningSent(true);
            reservationRepository.save(r);
        }

        // FR-07 part 2: at-expiry notification
        for (Reservation r : reservationRepository.findReachedExpiriesNeedingNotification(now)) {
            String msg = renderFor(r.getDriver(),
                    "notification.expiry.reached",
                    r.getParking().getName());
            record(r.getDriver(), r, Notification.NotificationType.EXPIRY_REACHED,
                    Notification.NotificationChannel.PUSH, msg);
            r.setExpiryReachedSent(true);
            reservationRepository.save(r);
        }
    }

    /**
     * FR-08: every 30 minutes, re-nag drivers whose paid session has lapsed
     * but they haven't extended/paid the overage. Limited to once per
     * UNPAID_REMINDER_HOURS to avoid spam.
     */
    @Scheduled(fixedRate = 30 * 60_000)
    @Transactional
    public void sendUnpaidReminders() {
        Instant now = Instant.now();
        for (Reservation r : reservationRepository.findExpiredConfirmed(now)) {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(UNPAID_REMINDER_HOURS);
            long recent = notificationRepository.countRecentByReservationAndType(
                    r.getId(), Notification.NotificationType.UNPAID_REMINDER, cutoff);
            if (recent > 0) continue;

            String msg = renderFor(r.getDriver(),
                    "notification.unpaid.reminder",
                    r.getParking().getName());
            record(r.getDriver(), r, Notification.NotificationType.UNPAID_REMINDER,
                    Notification.NotificationChannel.PUSH, msg);
        }
    }
}
