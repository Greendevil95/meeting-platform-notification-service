package com.example.notificationservice.kafka.user;

import com.example.notificationservice.config.properties.KafkaTopicsProperties;
import com.example.notificationservice.notification.NotificationService;
import com.example.notificationservice.user.UserReadModelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserEventsConsumerTest {

    @Mock
    private UserReadModelService userReadModelService;

    @Mock
    private NotificationService notificationService;

    @Test
    void malformedPayloadRemainsNonRetryableJacksonException() {
        KafkaTopicsProperties topics = new KafkaTopicsProperties();
        topics.setUserCreated("user-service.user-created");
        UserEventsConsumer consumer = new UserEventsConsumer(
                userReadModelService,
                notificationService,
                JsonMapper.builder().findAndAddModules().build(),
                topics
        );

        assertThrows(JacksonException.class, () -> consumer.onUserCreated("{broken"));
    }
}
