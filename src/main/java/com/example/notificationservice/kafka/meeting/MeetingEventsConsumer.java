package com.example.notificationservice.kafka.meeting;

import com.example.notificationservice.config.properties.KafkaTopicsProperties;
import com.example.notificationservice.meeting.MeetingProfile;
import com.example.notificationservice.meeting.MeetingReadModelService;
import com.example.notificationservice.notification.MeetingNotificationPayload;
import com.example.notificationservice.notification.NotificationAggregateType;
import com.example.notificationservice.notification.NotificationService;
import com.example.notificationservice.notification.NotificationType;
import com.example.notificationservice.user.UserProfile;
import com.example.notificationservice.user.UserReadModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class MeetingEventsConsumer {
    private final MeetingReadModelService meetingReadModelService;
    private final UserReadModelService userReadModelService;
    private final NotificationService notificationService;
    private final JsonMapper jsonMapper;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @KafkaListener(topics = "${notification.kafka.topics.meeting-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMeetingCreated(String payload) {
        consume(kafkaTopicsProperties.getMeetingCreated(), payload, () -> {
            MeetingCreatedEvent event = jsonMapper.readValue(payload, MeetingCreatedEvent.class);
            meetingReadModelService.upsertMeeting(new MeetingProfile(
                    event.meetingId(),
                    event.organizerId(),
                    event.title(),
                    event.description(),
                    event.startAt(),
                    event.endAt(),
                    Collections.emptySet(),
                    event.version(),
                    event.occurredAt()
            ));
        });
    }

    @KafkaListener(topics = "${notification.kafka.topics.meeting-updated}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMeetingUpdated(String payload) {
        consume(kafkaTopicsProperties.getMeetingUpdated(), payload, () -> {
            MeetingUpdatedEvent event = jsonMapper.readValue(payload, MeetingUpdatedEvent.class);
            meetingReadModelService.upsertMeeting(new MeetingProfile(
                    event.meetingId(),
                    event.organizerId(),
                    event.title(),
                    event.description(),
                    event.startAt(),
                    event.endAt(),
                    event.participantUserIds(),
                    event.version(),
                    event.occurredAt()
            ));

            for (UUID recipientUserId : event.participantUserIds()) {
                notificationService.createNotification(
                        recipientUserId,
                        NotificationType.MEETING_UPDATED,
                        event,
                        NotificationAggregateType.MEETING,
                        event.meetingId(),
                        event.eventId(),
                        findEmail(recipientUserId)
                );
            }
        });
    }

    @KafkaListener(topics = "${notification.kafka.topics.meeting-cancelled}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMeetingCancelled(String payload) {
        consume(kafkaTopicsProperties.getMeetingCancelled(), payload, () -> {
            MeetingCancelledEvent event = jsonMapper.readValue(payload, MeetingCancelledEvent.class);
            meetingReadModelService.findMeeting(event.meetingId()).ifPresent(meeting -> {
                for (UUID recipientUserId : meeting.participantUserIds()) {
                    notificationService.createNotification(
                            recipientUserId,
                            NotificationType.MEETING_CANCELLED,
                            new MeetingNotificationPayload(event.meetingId(), meeting.title(), meeting.startAt()),
                            NotificationAggregateType.MEETING,
                            event.meetingId(),
                            event.eventId(),
                            findEmail(recipientUserId)
                    );
                }
            });
        });
    }

    @KafkaListener(topics = "${notification.kafka.topics.meeting-participant-added}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMeetingParticipantAdded(String payload) {
        consume(kafkaTopicsProperties.getMeetingParticipantAdded(), payload, () -> {
            MeetingParticipantAddedEvent event = jsonMapper.readValue(payload, MeetingParticipantAddedEvent.class);
            MeetingProfile meeting = meetingReadModelService.findMeeting(event.meetingId()).orElse(null);
            notificationService.createNotification(
                    event.userId(),
                    NotificationType.MEETING_PARTICIPANT_ADDED,
                    toMeetingNotificationPayload(event.meetingId(), meeting),
                    NotificationAggregateType.MEETING,
                    event.meetingId(),
                    event.eventId(),
                    findEmail(event.userId())
            );
            meetingReadModelService.addParticipant(event.meetingId(), event.userId(), event.version(), event.occurredAt());
        });
    }

    @KafkaListener(topics = "${notification.kafka.topics.meeting-participant-removed}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMeetingParticipantRemoved(String payload) {
        consume(kafkaTopicsProperties.getMeetingParticipantRemoved(), payload, () -> {
            MeetingParticipantRemovedEvent event = jsonMapper.readValue(payload, MeetingParticipantRemovedEvent.class);
            MeetingProfile meeting = meetingReadModelService.findMeeting(event.meetingId()).orElse(null);
            notificationService.createNotification(
                    event.userId(),
                    NotificationType.MEETING_PARTICIPANT_REMOVED,
                    toMeetingNotificationPayload(event.meetingId(), meeting),
                    NotificationAggregateType.MEETING,
                    event.meetingId(),
                    event.eventId(),
                    findEmail(event.userId())
            );
            meetingReadModelService.removeParticipant(event.meetingId(), event.userId(), event.version(), event.occurredAt());
        });
    }

    private String findEmail(UUID userId) {
        return userReadModelService.findUser(userId)
                .map(UserProfile::email)
                .orElse(null);
    }

    private MeetingNotificationPayload toMeetingNotificationPayload(UUID meetingId, MeetingProfile meeting) {
        if (meeting == null) {
            return new MeetingNotificationPayload(meetingId, null, null);
        }
        return new MeetingNotificationPayload(meetingId, meeting.title(), meeting.startAt());
    }

    private void consume(String topic, String payload, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            log.error("Failed to process {} event: {}", topic, payload, ex);
            throw new IllegalStateException("Cannot process meeting event", ex);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
