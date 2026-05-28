package com.example.notificationservice.kafka.meeting;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record MeetingUpdatedEvent(
        UUID eventId,
        OffsetDateTime occurredAt,
        UUID meetingId,
        UUID organizerId,
        Set<UUID> participantUserIds,
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        long version
){}
