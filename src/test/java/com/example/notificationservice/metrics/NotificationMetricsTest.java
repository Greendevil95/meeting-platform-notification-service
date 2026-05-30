package com.example.notificationservice.metrics;

import com.example.notificationservice.notification.NotificationAggregateType;
import com.example.notificationservice.notification.NotificationType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationMetricsTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final NotificationMetrics notificationMetrics = new NotificationMetrics(meterRegistry);

    @Test
    void recordsNotificationCreatedWithStableTags() {
        notificationMetrics.recordNotificationCreated(NotificationType.MEETING_UPDATED, NotificationAggregateType.MEETING);

        assertEquals(
                1.0,
                meterRegistry.counter(
                        "app.notifications.created",
                        "type", "MEETING_UPDATED",
                        "aggregate_type", "MEETING"
                ).count()
        );
    }

    @Test
    void recordsEmailDeliveryCreated() {
        notificationMetrics.recordEmailDeliveryCreated(NotificationType.USER_CREATED);

        assertEquals(
                1.0,
                meterRegistry.counter("app.email_deliveries.created", "type", "USER_CREATED").count()
        );
    }

    @Test
    void recordsSkippedKafkaEvent() {
        notificationMetrics.recordKafkaEventSkipped("user", "stale_version");

        assertEquals(
                1.0,
                meterRegistry.counter(
                        "app.kafka.events.skipped",
                        "read_model", "user",
                        "reason", "stale_version"
                ).count()
        );
    }

    @Test
    void recordsDltRoutedWithoutExceptionMessage() {
        notificationMetrics.recordDltRouted(
                "meeting-service.meeting-updated",
                "notification-service.dlt.meeting-updated",
                new IllegalStateException("contains dynamic payload")
        );

        assertEquals(
                1.0,
                meterRegistry.counter(
                        "app.kafka.events.dlt.routed",
                        "original_topic", "meeting-service.meeting-updated",
                        "dlt_topic", "notification-service.dlt.meeting-updated",
                        "exception", "IllegalStateException"
                ).count()
        );
    }
}
