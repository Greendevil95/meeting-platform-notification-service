package com.example.notificationservice.kafka.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserDeletedEvent(
        UUID eventId,
        UUID userId,
        long version,
        OffsetDateTime timestamp
) {
}
