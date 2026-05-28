package com.example.notificationservice.kafka.meeting;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingParticipantRemovedEvent(
        UUID eventId,
        OffsetDateTime occurredAt,
        UUID meetingId,
        UUID userId,
        long version
){}
