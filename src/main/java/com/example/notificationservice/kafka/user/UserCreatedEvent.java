package com.example.notificationservice.kafka.user;

import com.example.notificationservice.user.UserRole;
import com.example.notificationservice.user.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserCreatedEvent(
        UUID eventId,
        UUID userId,
        String username,
        String email,
        UserStatus status,
        UserRole role,
        long version,
        OffsetDateTime timestamp
) {
}
