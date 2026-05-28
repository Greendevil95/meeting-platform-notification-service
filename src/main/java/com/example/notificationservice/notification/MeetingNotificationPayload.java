package com.example.notificationservice.notification;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingNotificationPayload(
        UUID meetingId,
        String title,
        OffsetDateTime startAt
) {
}
