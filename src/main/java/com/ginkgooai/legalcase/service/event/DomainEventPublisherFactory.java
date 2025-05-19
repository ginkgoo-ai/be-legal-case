package com.ginkgooai.legalcase.service.event;

import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.domain.event.DomainEvent;
import com.ginkgooai.legalcase.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for creating and managing domain event publishers
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisherFactory {

	private final EventPublisher eventPublisher;

	/**
	 * Get the event publisher instance
	 * @return event publisher
	 */
	public EventPublisher getEventPublisher() {
		return eventPublisher;
	}

	/**
	 * Publish all domain events from a legal case
	 * @param legalCase legal case with domain events
	 */
	public void publishEvents(LegalCase legalCase) {
		List<DomainEvent> events = legalCase.getAndClearDomainEvents();
		if (events.isEmpty()) {
			return;
		}

		log.debug("Publishing {} domain events for case: {}", events.size(), legalCase.getId());
		events.forEach(eventPublisher::publish);
	}
}