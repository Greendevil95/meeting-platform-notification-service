package com.example.notificationservice.metrics;

import com.example.notificationservice.notification.NotificationAggregateType;
import com.example.notificationservice.notification.NotificationChannel;
import com.example.notificationservice.notification.NotificationType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetrics {

    private static final String NOTIFICATIONS_CREATED = "app.notifications.created";
    private static final String NOTIFICATION_DELIVERIES_CREATED = "app.notification_deliveries.created";
    private static final String KAFKA_EVENTS_SKIPPED = "app.kafka.events.skipped";
    private static final String KAFKA_EVENTS_DLT_ROUTED = "app.kafka.events.dlt.routed";

    private final MeterRegistry meterRegistry;

    public NotificationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordNotificationCreated(NotificationType type, NotificationAggregateType aggregateType) {
        meterRegistry.counter(
                NOTIFICATIONS_CREATED,
                Tags.of("type", type.name(), "aggregate_type", aggregateType.name())
        ).increment();
    }

    public void recordNotificationDeliveryCreated(NotificationType type, NotificationChannel channel) {
        meterRegistry.counter(
                NOTIFICATION_DELIVERIES_CREATED,
                Tags.of("type", type.name(), "channel", channel.name())
        ).increment();
    }

    public void recordKafkaEventSkipped(String readModel, String reason) {
        meterRegistry.counter(KAFKA_EVENTS_SKIPPED, Tags.of("read_model", readModel, "reason", reason)).increment();
    }

    public void recordDltRouted(String originalTopic, String dltTopic, Exception exception) {
        meterRegistry.counter(
                KAFKA_EVENTS_DLT_ROUTED,
                Tags.of(
                        "original_topic", originalTopic,
                        "dlt_topic", dltTopic,
                        "exception", exception.getClass().getSimpleName()
                )
        ).increment();
    }
}
