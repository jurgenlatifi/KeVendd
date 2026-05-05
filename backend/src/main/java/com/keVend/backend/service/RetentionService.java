package com.keVend.backend.service;

import com.keVend.backend.model.Reservation;
import com.keVend.backend.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * NFR-21 / NFR-22 retention enforcement. Runs nightly at 03:00 server time.
 * The configured floors are *minimums*; we keep data at least that long, then
 * purge once it exceeds the configured horizon.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetentionService {

    private final ReservationRepository reservationRepository;
    private final EntityManager entityManager;

    /** NFR-21 driver history minimum + NFR-22 owner data minimum. Default 24 months satisfies both. */
    @Value("${app.retention.reservation-months:24}")
    private int reservationRetentionMonths;

    /** Notifications are operational data, retained much shorter. */
    @Value("${app.retention.notification-months:6}")
    private int notificationRetentionMonths;

    /** Hard delete reservations + cascading payments older than the retention horizon. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeOldReservations() {
        Instant cutoff = Instant.now().minus(reservationRetentionMonths * 30L, ChronoUnit.DAYS);
        var toPurge = reservationRepository.findAll().stream()
                .filter(r -> r.getEndTime() != null && r.getEndTime().isBefore(cutoff))
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED
                          || r.getStatus() == Reservation.ReservationStatus.CANCELLED
                          || r.getStatus() == Reservation.ReservationStatus.EXPIRED)
                .toList();

        if (toPurge.isEmpty()) return;

        // Payments cascade via FK-on-delete in DB; if you've not added the constraint,
        // delete payments for these reservation IDs explicitly first via JPQL.
        int deletedPayments = entityManager.createQuery(
                "DELETE FROM Payment p WHERE p.reservation IN :rs")
                .setParameter("rs", toPurge)
                .executeUpdate();

        int deletedReviews = entityManager.createQuery(
                "DELETE FROM Review r WHERE r.reservation IN :rs")
                .setParameter("rs", toPurge)
                .executeUpdate();

        int deletedNotifications = entityManager.createQuery(
                "DELETE FROM Notification n WHERE n.reservation IN :rs")
                .setParameter("rs", toPurge)
                .executeUpdate();

        reservationRepository.deleteAll(toPurge);

        log.info("[retention] purged {} reservations + {} payments + {} reviews + {} notifications older than {} months",
                toPurge.size(), deletedPayments, deletedReviews, deletedNotifications, reservationRetentionMonths);
    }

    /** Drops aged notifications + revoked-and-expired refresh tokens to keep the auth tables small. */
    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void purgeOperationalData() {
        LocalDateTime notifCutoff = LocalDateTime.now().minusMonths(notificationRetentionMonths);
        int notifs = entityManager.createQuery(
                "DELETE FROM Notification n WHERE n.createdAt < :cutoff")
                .setParameter("cutoff", notifCutoff)
                .executeUpdate();

        int tokens = entityManager.createQuery(
                "DELETE FROM RefreshToken t WHERE t.expiresAt < :now")
                .setParameter("now", Instant.now())
                .executeUpdate();

        int verifs = entityManager.createQuery(
                "DELETE FROM EmailVerificationToken v WHERE v.expiresAt < :now OR v.used = true")
                .setParameter("now", Instant.now())
                .executeUpdate();

        log.info("[retention] purged {} notifications, {} expired refresh tokens, {} used/expired email tokens",
                notifs, tokens, verifs);
    }

    /** Used by tests / admin tooling. */
    public Instant currentReservationCutoff() {
        return LocalDateTime.now().minusMonths(reservationRetentionMonths)
                .toInstant(ZoneOffset.UTC);
    }
}
