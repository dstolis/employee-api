package com.dstolis.employeeapi.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dstolis.employeeapi.model.entity.OutboxEvent;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findAllByStatus(OutboxEvent.Status status);
}
