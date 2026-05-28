package com.example.notificationservice.kafka.meeting;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingCancelledEvent(
        UUID eventId,
        OffsetDateTime occurredAt,
        UUID meetingId,
        UUID requestorId,
        long version
){}
