package com.example.notificationservice.config;

import com.example.notificationservice.metrics.NotificationMetrics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationDltDestinationResolverTest {

    private final NotificationMetrics notificationMetrics = mock(NotificationMetrics.class);
    private final NotificationDltDestinationResolver resolver =
            new NotificationDltDestinationResolver("notification-service", notificationMetrics);

    @Test
    void resolvesUserEventToServiceSpecificDltTopic() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("user-service.user-created", 1, 42L, "user-id", "{}");
        IllegalStateException exception = new IllegalStateException("boom");

        TopicPartition destination = resolver.apply(record, exception);

        assertEquals("notification-service.dlt.user-created", destination.topic());
        assertEquals(1, destination.partition());
        verify(notificationMetrics).recordDltRouted(
                "user-service.user-created",
                "notification-service.dlt.user-created",
                exception
        );
    }

    @Test
    void resolvesMeetingEventToServiceSpecificDltTopic() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("meeting-service.meeting-participant-added", 2, 77L, "meeting-id", "{}");

        TopicPartition destination = resolver.apply(record, new IllegalStateException("boom"));

        assertEquals("notification-service.dlt.meeting-participant-added", destination.topic());
        assertEquals(2, destination.partition());
    }
}
