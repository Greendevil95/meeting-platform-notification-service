package com.example.notificationservice.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserReadModelRepository extends JpaRepository<UserReadModelEntity, UUID> {
}
