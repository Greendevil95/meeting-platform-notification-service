package com.example.notificationservice.kafka.meeting;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingCreatedEvent(
        UUID eventId,
        OffsetDateTime occurredAt,
        UUID meetingId,
        UUID organizerId,
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        long version
) {
}
