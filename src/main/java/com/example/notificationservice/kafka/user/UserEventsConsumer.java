package com.example.notificationservice.kafka.user;

import com.example.notificationservice.config.properties.KafkaTopicsProperties;
import com.example.notificationservice.notification.NotificationAggregateType;
import com.example.notificationservice.notification.NotificationService;
import com.example.notificationservice.notification.NotificationType;
import com.example.notificationservice.user.UserProfile;
import com.example.notificationservice.user.UserReadModelService;
import com.example.notificationservice.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Slf4j
@Component
public class UserEventsConsumer {
    private final UserReadModelService userReadModelService;
    private final NotificationService notificationService;
    private final JsonMapper jsonMapper;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @KafkaListener(topics = "${notification.kafka.topics.user-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void onUserCreated(String payload) {
        consume(kafkaTopicsProperties.getUserCreated(), payload, () -> {
            UserCreatedEvent event = jsonMapper.readValue(payload, UserCreatedEvent.class);
            userReadModelService.upsertUser(new UserProfile(
                    event.userId(),
                    event.username(),
                    event.email(),
                    event.status(),
                    event.role(),
                    event.version(),
                    event.timestamp()
            ));
            notificationService.createNotification(
                    event.userId(),
                    NotificationType.USER_CREATED,
                    event,
                    NotificationAggregateType.USER,
                    event.userId(),
                    event.eventId(),
                    event.email()
            );
        });
    }

    @KafkaListener(topics = "${notification.kafka.topics.user-updated}", groupId = "${spring.kafka.consumer.group-id}")
    public void onUserUpdated(String payload) {
        consume(kafkaTopicsProperties.getUserUpdated(), payload, () -> {
            UserUpdatedEvent event = jsonMapper.readValue(payload, UserUpdatedEvent.class);
            userReadModelService.upsertUser(new UserProfile(
                    event.userId(),
                    event.username(),
                    event.email(),
                    event.status(),
                    event.role(),
                    event.version(),
                    event.timestamp()
            ));
            notificationService.createNotification(
                    event.userId(),
                    NotificationType.USER_UPDATED,
                    event,
                    NotificationAggregateType.USER,
                    event.userId(),
                    event.eventId(),
                    event.email()
            );
        });
    }

    @KafkaListener(topics = "${notification.kafka.topics.user-deleted}", groupId = "${spring.kafka.consumer.group-id}")
    public void onUserDeleted(String payload) {
        consume(kafkaTopicsProperties.getUserDeleted(), payload, () -> {
            UserDeletedEvent event = jsonMapper.readValue(payload, UserDeletedEvent.class);
            String email = userReadModelService.findUser(event.userId())
                    .map(UserProfile::email)
                    .orElse(null);
            userReadModelService.updateStatus(event.userId(), UserStatus.DELETED, event.version(), event.timestamp());
            notificationService.createNotification(
                    event.userId(),
                    NotificationType.USER_DELETED,
                    event,
                    NotificationAggregateType.USER,
                    event.userId(),
                    event.eventId(),
                    email
            );
        });
    }

    @KafkaListener(topics = "${notification.kafka.topics.user-status-changed}", groupId = "${spring.kafka.consumer.group-id}")
    public void onUserStatusChanged(String payload) {
        consume(kafkaTopicsProperties.getUserStatusChanged(), payload, () -> {
            UserStatusChangedEvent event = jsonMapper.readValue(payload, UserStatusChangedEvent.class);
            String email = userReadModelService.findUser(event.userId())
                    .map(UserProfile::email)
                    .orElse(null);
            userReadModelService.updateStatus(event.userId(), event.status(), event.version(), event.timestamp());
            notificationService.createNotification(
                    event.userId(),
                    NotificationType.USER_STATUS_CHANGED,
                    event,
                    NotificationAggregateType.USER,
                    event.userId(),
                    event.eventId(),
                    email
            );
        });
    }

    private void consume(String topic, String payload, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            log.error("Failed to process {} event: {}", topic, payload, ex);
            throw new IllegalStateException("Cannot process user event", ex);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
