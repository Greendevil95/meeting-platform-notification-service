package com.example.notificationservice.meeting;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record MeetingProfile(
        UUID meetingId,
        UUID organizerId,
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        Set<UUID> participantUserIds,
        long version,
        OffsetDateTime updatedAt
) {
}
