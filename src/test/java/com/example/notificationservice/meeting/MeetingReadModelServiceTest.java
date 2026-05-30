package com.example.notificationservice.meeting;

import com.example.notificationservice.metrics.NotificationMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingReadModelServiceTest {

    @Mock
    private MeetingReadModelRepository repository;

    @Mock
    private MeetingReadModelMapper mapper;

    @Mock
    private NotificationMetrics notificationMetrics;

    @Test
    void recordsSkippedMetricForStaleParticipantEvent() {
        UUID meetingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MeetingReadModelEntity entity = new MeetingReadModelEntity();
        entity.setMeetingId(meetingId);
        entity.setVersion(5);
        entity.setParticipantUserIds(Set.of());
        when(repository.findById(meetingId)).thenReturn(Optional.of(entity));
        MeetingReadModelService service = new MeetingReadModelService(repository, mapper, notificationMetrics);

        service.addParticipant(meetingId, userId, 4, OffsetDateTime.now());

        verify(notificationMetrics).recordKafkaEventSkipped("meeting", "stale_version");
        verify(repository, never()).save(any());
    }

    @Test
    void recordsSkippedMetricForStaleUpsertEvent() {
        UUID meetingId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        MeetingReadModelEntity entity = new MeetingReadModelEntity();
        entity.setMeetingId(meetingId);
        entity.setVersion(2);
        MeetingProfile staleProfile = new MeetingProfile(
                meetingId,
                UUID.randomUUID(),
                "Planning",
                "Weekly planning",
                now,
                now.plusHours(1),
                Set.of(UUID.randomUUID()),
                2,
                now
        );
        when(repository.findById(meetingId)).thenReturn(Optional.of(entity));
        MeetingReadModelService service = new MeetingReadModelService(repository, mapper, notificationMetrics);

        service.upsertMeeting(staleProfile);

        verify(notificationMetrics).recordKafkaEventSkipped("meeting", "stale_version");
        verify(mapper, never()).updateEntity(any(), any());
        verify(repository, never()).save(any());
    }
}
