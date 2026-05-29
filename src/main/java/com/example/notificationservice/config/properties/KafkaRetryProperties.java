package com.example.notificationservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "notification.kafka.retry")
@Getter
@Setter
public class KafkaRetryProperties {

    private int maxRetries = 3;
    private Duration initialInterval = Duration.ofSeconds(1);
    private double multiplier = 2.0;
    private Duration maxInterval = Duration.ofSeconds(10);
}
