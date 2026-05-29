package com.example.notificationservice.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationDltDestinationResolverTest {

    private final NotificationDltDestinationResolver resolver =
            new NotificationDltDestinationResolver("notification-service");

    @Test
    void resolvesUserEventToServiceSpecificDltTopic() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("user-service.user-created", 1, 42L, "user-id", "{}");

        TopicPartition destination = resolver.apply(record, new IllegalStateException("boom"));

        assertEquals("notification-service.dlt.user-created", destination.topic());
        assertEquals(1, destination.partition());
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
