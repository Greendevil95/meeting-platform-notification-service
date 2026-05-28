package com.example.notificationservice.notification;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class NotificationContentBuilder {

    private static final DateTimeFormatter MEETING_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public NotificationContent build(NotificationType type, Map<String, Object> payload) {
        return switch (type) {
            case USER_CREATED -> new NotificationContent("Profile created", "Your profile has been created.");
            case USER_UPDATED -> new NotificationContent("Profile updated", "Your profile details have been updated.");
            case USER_STATUS_CHANGED -> new NotificationContent(
                    "Profile status changed",
                    "Your profile status has changed to " + payload.get("status") + "."
            );
            case USER_DELETED -> new NotificationContent("Profile deleted", "Your profile has been marked as deleted.");
            case MEETING_UPDATED -> new NotificationContent(
                    "Meeting updated",
                    "Meeting \"" + payload.get("title") + "\" has been updated. Start time: " + formatStartAt(payload.get("startAt")) + "."
            );
            case MEETING_CANCELLED -> new NotificationContent(
                    "Meeting cancelled",
                    "Meeting \"" + payload.get("title") + "\" scheduled for " + formatStartAt(payload.get("startAt")) + " has been cancelled."
            );
            case MEETING_PARTICIPANT_ADDED -> buildParticipantContent(
                    "Added to meeting",
                    "You have been added to meeting \"%s\" scheduled for %s.",
                    "You have been added to a meeting.",
                    payload
            );
            case MEETING_PARTICIPANT_REMOVED -> buildParticipantContent(
                    "Removed from meeting",
                    "You have been removed from meeting \"%s\" scheduled for %s.",
                    "You have been removed from a meeting.",
                    payload
            );
        };
    }

    private NotificationContent buildParticipantContent(
            String title,
            String template,
            String fallbackMessage,
            Map<String, Object> payload
    ) {
        Object meetingTitle = payload.get("title");
        Object startAt = payload.get("startAt");
        if (meetingTitle == null || startAt == null) {
            return new NotificationContent(title, fallbackMessage);
        }
        return new NotificationContent(title, template.formatted(meetingTitle, formatStartAt(startAt)));
    }

    private String formatStartAt(Object value) {
        if (value == null) {
            return "unknown";
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.format(MEETING_TIME_FORMATTER);
        }
        return OffsetDateTime.parse(value.toString()).format(MEETING_TIME_FORMATTER);
    }
}
