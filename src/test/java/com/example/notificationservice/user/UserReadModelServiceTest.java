package com.example.notificationservice.user;

import com.example.notificationservice.metrics.NotificationMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserReadModelServiceTest {

    @Mock
    private UserReadModelRepository repository;

    @Mock
    private UserReadModelMapper mapper;

    @Mock
    private NotificationMetrics notificationMetrics;

    @Test
    void recordsSkippedMetricForStaleStatusEvent() {
        UUID userId = UUID.randomUUID();
        UserReadModelEntity entity = new UserReadModelEntity();
        entity.setUserId(userId);
        entity.setVersion(3);
        when(repository.findById(userId)).thenReturn(Optional.of(entity));
        UserReadModelService service = new UserReadModelService(repository, mapper, notificationMetrics);

        service.updateStatus(userId, UserStatus.ACTIVE, 2, OffsetDateTime.now());

        verify(notificationMetrics).recordKafkaEventSkipped("user", "stale_version");
        verify(repository, never()).save(any());
    }

    @Test
    void recordsSkippedMetricForStaleUpsertEvent() {
        UUID userId = UUID.randomUUID();
        UserReadModelEntity entity = new UserReadModelEntity();
        entity.setUserId(userId);
        entity.setVersion(7);
        UserProfile staleProfile = new UserProfile(
                userId,
                "denis",
                "denis@example.com",
                UserStatus.ACTIVE,
                UserRole.USER,
                7,
                OffsetDateTime.now()
        );
        when(repository.findById(userId)).thenReturn(Optional.of(entity));
        UserReadModelService service = new UserReadModelService(repository, mapper, notificationMetrics);

        service.upsertUser(staleProfile);

        verify(notificationMetrics).recordKafkaEventSkipped("user", "stale_version");
        verify(mapper, never()).updateEntity(any(), any());
        verify(repository, never()).save(any());
    }
}
