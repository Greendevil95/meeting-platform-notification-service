package com.example.notificationservice.notification;

import com.example.notificationservice.metrics.NotificationMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDeliveryRepository notificationDeliveryRepository;

    @Mock
    private NotificationMetrics notificationMetrics;

    private final NotificationContentBuilder notificationContentBuilder = new NotificationContentBuilder();
    private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void recordsMetricsWhenNotificationAndDeliveryAreCreated() {
        NotificationService service = new NotificationService(
                notificationRepository,
                notificationDeliveryRepository,
                notificationContentBuilder,
                jsonMapper,
                notificationMetrics
        );
        UUID recipientUserId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        UUID sourceEventId = UUID.randomUUID();
        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.createNotification(
                recipientUserId,
                NotificationType.USER_CREATED,
                null,
                NotificationAggregateType.USER,
                aggregateId,
                sourceEventId,
                "user@example.com"
        );

        verify(notificationMetrics).recordNotificationCreated(NotificationType.USER_CREATED, NotificationAggregateType.USER);
        verify(notificationMetrics).recordNotificationDeliveryCreated(NotificationType.USER_CREATED, NotificationChannel.EMAIL);
        ArgumentCaptor<NotificationDeliveryEntity> deliveryCaptor = ArgumentCaptor.forClass(NotificationDeliveryEntity.class);
        verify(notificationDeliveryRepository).save(deliveryCaptor.capture());
        assertEquals(NotificationChannel.EMAIL, deliveryCaptor.getValue().getChannel());
        assertEquals("user@example.com", deliveryCaptor.getValue().getDestination());
    }

    @Test
    void doesNotRecordMetricsForDuplicateNotification() {
        NotificationService service = new NotificationService(
                notificationRepository,
                notificationDeliveryRepository,
                notificationContentBuilder,
                jsonMapper,
                notificationMetrics
        );
        UUID recipientUserId = UUID.randomUUID();
        UUID sourceEventId = UUID.randomUUID();
        when(notificationRepository.existsBySourceEventIdAndRecipientUserIdAndType(
                sourceEventId,
                recipientUserId,
                NotificationType.USER_UPDATED
        )).thenReturn(true);

        service.createNotification(
                recipientUserId,
                NotificationType.USER_UPDATED,
                null,
                NotificationAggregateType.USER,
                UUID.randomUUID(),
                sourceEventId,
                "user@example.com"
        );

        verify(notificationRepository, never()).save(any());
        verify(notificationDeliveryRepository, never()).save(any());
        verify(notificationMetrics, never()).recordNotificationCreated(any(), any());
        verify(notificationMetrics, never()).recordNotificationDeliveryCreated(any(), any());
    }
}
