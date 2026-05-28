package com.example.notificationservice.meeting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MeetingReadModelRepository extends JpaRepository<MeetingReadModelEntity, UUID> {
}
