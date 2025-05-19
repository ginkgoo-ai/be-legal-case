package com.ginkgooai.legalcase.service.event;

import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.domain.event.DomainEvent;
import com.ginkgooai.legalcase.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for publishing domain events from aggregates
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisherFactory {

	private final EventPublisher eventPublisher;

	/**
	 * 获取事件发布器实例 Get the event publisher instance
	 * @return 事件发布器 / event publisher
	 */
	public EventPublisher getEventPublisher() {
		return eventPublisher;
	}

	/**
	 * Publish all domain events from a legal case
	 * @param legalCase the legal case
	 */
	public void publishEvents(LegalCase legalCase) {
		List<DomainEvent> events = legalCase.getAndClearDomainEvents();

		if (events.isEmpty()) {
			return;
		}

		log.debug("Publishing {} events for case {}", events.size(), legalCase.getId());

		events.forEach(eventPublisher::publish);
	}

}