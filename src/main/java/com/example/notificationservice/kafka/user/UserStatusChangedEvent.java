package com.example.notificationservice.kafka.user;

import com.example.notificationservice.user.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserStatusChangedEvent(
        UUID eventId,
        UUID userId,
        UserStatus previousStatus,
        UserStatus status,
        long version,
        OffsetDateTime timestamp
) {
}
