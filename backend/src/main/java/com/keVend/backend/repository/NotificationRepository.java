package com.keVend.backend.repository;

import com.keVend.backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** FR-08: prevents resending an unpaid reminder for the same reservation more often than every {@code minIntervalHours}. */
    @Query("""
            SELECT COUNT(n) FROM Notification n
            WHERE n.reservation.id = :reservationId
              AND n.type = :type
              AND n.createdAt >= :since
            """)
    long countRecentByReservationAndType(
            @Param("reservationId") Long reservationId,
            @Param("type") Notification.NotificationType type,
            @Param("since") LocalDateTime since);
}
