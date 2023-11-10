
package com.dstolis.employeeapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.dstolis.employeeapi.model.entity.OutboxEvent;
import com.dstolis.employeeapi.repository.OutboxRepository;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private OutboxRepository outboxRepository;

    @InjectMocks
    private PublisherService publisherService;

    @Test
    void publishEvents_whenThereArePendingEvents_shouldPublishEachEvent() {
        var eventId = UUID.randomUUID();
        var createdEvent = new OutboxEvent(eventId, "Employee", "CREATED", "Event Payload", OffsetDateTime.now(), OutboxEvent.Status.PENDING);
        var updatedEvent = new OutboxEvent(eventId, "Employee", "UPDATED", "Event Payload", OffsetDateTime.now(), OutboxEvent.Status.PENDING);
        var deletedEvent = new OutboxEvent(eventId, "Employee", "DELETED", "Event Payload", OffsetDateTime.now(), OutboxEvent.Status.PENDING);

        var pendingEvents = List.of(createdEvent, updatedEvent, deletedEvent);

        when(outboxRepository.findAllByStatus(OutboxEvent.Status.PENDING)).thenReturn(pendingEvents);

        // Act
        publisherService.publishEvents();

        // Assert
        verify(outboxRepository, times(1)).findAllByStatus(OutboxEvent.Status.PENDING);
        verify(kafkaTemplate, times(3)).send("employee-events", "Event Payload");
        verify(outboxRepository, times(3)).save(any(OutboxEvent.class));

        assertEquals(OutboxEvent.Status.PROCESSED, createdEvent.getStatus());
        assertEquals(OutboxEvent.Status.PROCESSED, updatedEvent.getStatus());
        assertEquals(OutboxEvent.Status.PROCESSED, deletedEvent.getStatus());
    }
}
