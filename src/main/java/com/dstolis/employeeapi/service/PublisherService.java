package com.dstolis.employeeapi.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dstolis.employeeapi.model.entity.OutboxEvent;
import com.dstolis.employeeapi.repository.OutboxRepository;

import jakarta.transaction.Transactional;

@Service
@EnableScheduling
public class PublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "employee-events";

    private final OutboxRepository outboxRepository;

    @Autowired
    public PublisherService(final KafkaTemplate<String, String> kafkaTemplate,
        final OutboxRepository outboxRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    @Transactional()
    public void publishEvents() {
        List<OutboxEvent> events = outboxRepository.findAllByStatus(OutboxEvent.Status.PENDING);

        for (OutboxEvent event : events) {
            kafkaTemplate.send(TOPIC, event.getPayload());
            event.setStatus(OutboxEvent.Status.PROCESSED);
            outboxRepository.save(event);
        }
    }
}
