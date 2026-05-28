package com.example.notificationservice.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailDeliveryRepository extends JpaRepository<EmailDeliveryEntity, UUID> {
}
