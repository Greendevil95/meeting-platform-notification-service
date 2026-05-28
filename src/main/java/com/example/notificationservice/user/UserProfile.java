package com.example.notificationservice.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfile(
        UUID userId,
        String username,
        String email,
        UserStatus status,
        UserRole role,
        long version,
        OffsetDateTime updatedAt
) {
}
