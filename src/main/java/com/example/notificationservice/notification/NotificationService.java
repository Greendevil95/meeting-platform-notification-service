package com.example.notificationservice.notification;

import com.example.notificationservice.metrics.NotificationMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailDeliveryRepository emailDeliveryRepository;
    private final NotificationContentBuilder notificationContentBuilder;
    private final JsonMapper jsonMapper;
    private final NotificationMetrics notificationMetrics;

    @Transactional
    public void createNotification(
            UUID recipientUserId,
            NotificationType type,
            Object payload,
            NotificationAggregateType aggregateType,
            UUID aggregateId,
            UUID sourceEventId,
            String email
    ) {
        if (notificationRepository.existsBySourceEventIdAndRecipientUserIdAndType(sourceEventId, recipientUserId, type)) {
            return;
        }

        Map<String, Object> payloadMap = toPayloadMap(payload);
        NotificationContent content = notificationContentBuilder.build(type, payloadMap == null ? Map.of() : payloadMap);

        NotificationEntity notification = new NotificationEntity();
        notification.setRecipientUserId(recipientUserId);
        notification.setType(type);
        notification.setTitle(content.title());
        notification.setMessage(content.message());
        notification.setPayload(payloadMap);
        notification.setAggregateType(aggregateType);
        notification.setAggregateId(aggregateId);
        notification.setSourceEventId(sourceEventId);
        notification.setRead(false);

        NotificationEntity savedNotification = notificationRepository.save(notification);
        notificationMetrics.recordNotificationCreated(type, aggregateType);

        if (email != null && !email.isBlank()) {
            EmailDeliveryEntity emailDelivery = new EmailDeliveryEntity();
            emailDelivery.setNotification(savedNotification);
            emailDelivery.setChannel(NotificationChannel.EMAIL);
            emailDelivery.setEmail(email);
            emailDelivery.setStatus(NotificationDeliveryStatus.PENDING);
            emailDeliveryRepository.save(emailDelivery);
            notificationMetrics.recordEmailDeliveryCreated(type);
        }
    }

    private Map<String, Object> toPayloadMap(Object payload) {
        if (payload == null) {
            return null;
        }

        try {
            return jsonMapper.convertValue(payload, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot serialize notification payload", ex);
        }
    }
}
