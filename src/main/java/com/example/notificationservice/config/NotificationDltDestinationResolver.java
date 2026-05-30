package com.example.notificationservice.config;

import com.example.notificationservice.metrics.NotificationMetrics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

import java.util.function.BiFunction;

public class NotificationDltDestinationResolver implements BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> {

    private final String consumerGroupId;
    private final NotificationMetrics notificationMetrics;

    public NotificationDltDestinationResolver(String consumerGroupId, NotificationMetrics notificationMetrics) {
        this.consumerGroupId = consumerGroupId;
        this.notificationMetrics = notificationMetrics;
    }

    @Override
    public TopicPartition apply(ConsumerRecord<?, ?> record, Exception exception) {
        var destination = new TopicPartition(
                consumerGroupId + ".dlt." + eventName(record.topic()),
                record.partition()
        );
        notificationMetrics.recordDltRouted(record.topic(), destination.topic(), exception);
        return destination;
    }

    private String eventName(String topic) {
        int delimiter = topic.indexOf('.');
        if (delimiter < 0 || delimiter == topic.length() - 1) {
            return topic;
        }
        return topic.substring(delimiter + 1);
    }
}
