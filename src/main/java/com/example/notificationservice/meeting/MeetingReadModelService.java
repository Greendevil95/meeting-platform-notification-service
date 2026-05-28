package com.example.notificationservice.meeting;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class MeetingReadModelService {

    private final MeetingReadModelRepository repository;
    private final MeetingReadModelMapper mapper;

    public MeetingReadModelService(
            MeetingReadModelRepository repository,
            MeetingReadModelMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public MeetingProfile upsertMeeting(MeetingProfile profile) {
        repository.findById(profile.meetingId())
                .ifPresentOrElse(existing -> applyIfNewerVersion(existing, profile), () -> create(profile));
        return profile;
    }

    @Transactional
    public void addParticipant(UUID meetingId, UUID userId, long version, OffsetDateTime eventTime) {
        repository.findById(meetingId).ifPresent(entity -> {
            if (version <= entity.getVersion()) {
                return;
            }

            Set<UUID> participants = new LinkedHashSet<>(
                    Objects.requireNonNullElse(
                            entity.getParticipantUserIds(), Collections.emptyList()
                    )
            );
            participants.add(userId);
            entity.setParticipantUserIds(participants);
            entity.setVersion(version);
            entity.setUpdatedAt(eventTime);
            repository.save(entity);
        });
    }

    @Transactional
    public void removeParticipant(UUID meetingId, UUID userId, long version, OffsetDateTime eventTime) {
        repository.findById(meetingId).ifPresent(entity -> {
            if (version <= entity.getVersion()) {
                return;
            }

            Set<UUID> participants = new LinkedHashSet<>(
                    Objects.requireNonNullElse(
                            entity.getParticipantUserIds(), Collections.emptyList()
                    )
            );
            participants.remove(userId);
            entity.setParticipantUserIds(participants);
            entity.setVersion(version);
            entity.setUpdatedAt(eventTime);
            repository.save(entity);
        });
    }

    @Transactional(readOnly = true)
    public Optional<MeetingProfile> findMeeting(UUID meetingId) {
        return repository.findById(meetingId).map(mapper::toProfile);
    }

    private void create(MeetingProfile profile) {
        repository.save(mapper.toEntity(profile));
    }

    private void applyIfNewerVersion(MeetingReadModelEntity existing, MeetingProfile profile) {
        if (profile.version() <= existing.getVersion()) {
            return;
        }
        mapper.updateEntity(profile, existing);
        repository.save(existing);
    }
}
