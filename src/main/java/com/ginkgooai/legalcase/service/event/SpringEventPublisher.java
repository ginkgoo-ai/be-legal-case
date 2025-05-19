package com.ginkgooai.legalcase.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.domain.event.CaseEvents;
import com.ginkgooai.legalcase.domain.event.DomainEvent;
import com.ginkgooai.legalcase.domain.event.EventLog;
import com.ginkgooai.legalcase.domain.event.EventPublisher;
import com.ginkgooai.legalcase.exception.EventPersistenceException;
import com.ginkgooai.legalcase.repository.EventLogRepository;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.CaseEventEmitterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SpringEventPublisher implements EventPublisher {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final EventLogRepository eventLogRepository;

	private final LegalCaseRepository legalCaseRepository;

	private final ObjectMapper objectMapper;

	private final CaseEventEmitterService eventEmitterService;

	public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher,
			EventLogRepository eventLogRepository, LegalCaseRepository legalCaseRepository, ObjectMapper objectMapper,
			@Lazy CaseEventEmitterService eventEmitterService) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.eventLogRepository = eventLogRepository;
		this.legalCaseRepository = legalCaseRepository;
		this.objectMapper = objectMapper;
		this.eventEmitterService = eventEmitterService;
	}

	@Override
	@Transactional
	public void publish(DomainEvent event) {
		log.debug("Publishing domain event: {}", event.getEventType());

		applicationEventPublisher.publishEvent(event);

		EventLog persistedEvent = persistEvent(event);

		if (persistedEvent != null) {
			eventEmitterService.handleDomainEvent(event);
		}
	}

	@Override
	@Transactional
	public void publishEvent(CaseEvents.FormValueRecordedEvent event) {
		log.debug("Publishing form value recorded event: {}", event.getEventId());

		applicationEventPublisher.publishEvent(event);

		EventLog persistedEvent = persistFormValueEvent(event);

		if (persistedEvent != null) {
			eventEmitterService.handleDomainEvent(event);
		}
	}

	private EventLog persistEvent(DomainEvent event) {
		try {
			String eventData = objectMapper.writeValueAsString(event);
			String caseId = event.getCaseId();

			if (caseId == null) {
				log.warn("Cannot determine case ID for event: {}", event.getEventType());
				return null;
			}

			LegalCase legalCase = legalCaseRepository.findById(caseId).orElse(null);

			if (legalCase == null) {
				log.warn("Cannot find case with ID: {} for event: {}", caseId, event.getEventType());
				return null;
			}

			EventLog eventLog = EventLog.builder()
				.legalCase(legalCase)
				.eventId(event.getEventId())
				.eventType(event.getEventType())
				.occurredAt(event.getOccurredAt())
				.eventData(eventData)
				.build();

			eventLogRepository.save(eventLog);
			return eventLog;

		}
		catch (JsonProcessingException e) {
			log.error("Error serializing event to JSON", e);
			throw new EventPersistenceException("Error persisting event: " + e.getMessage());
		}
	}

	private EventLog persistFormValueEvent(CaseEvents.FormValueRecordedEvent event) {
		try {
			String eventData = objectMapper.writeValueAsString(event);

			LegalCase legalCase = legalCaseRepository.findById(event.getCaseId()).orElse(null);

			if (legalCase == null) {
				log.warn("Cannot find case with ID: {} for form event", event.getCaseId());
				return null;
			}

			EventLog eventLog = EventLog.builder()
				.legalCase(legalCase)
				.eventId(event.getEventId())
				.eventType(event.getEventType())
				.occurredAt(event.getOccurredAt())
				.eventData(eventData)
				.build();

			eventLogRepository.save(eventLog);
			return eventLog;

		}
		catch (JsonProcessingException e) {
			log.error("Error serializing form event to JSON", e);
			throw new EventPersistenceException("Error persisting form event: " + e.getMessage());
		}
	}

}