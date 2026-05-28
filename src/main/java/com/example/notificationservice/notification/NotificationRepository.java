package com.example.notificationservice.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    boolean existsBySourceEventIdAndRecipientUserIdAndType(UUID sourceEventId, UUID recipientUserId, NotificationType type);
}
