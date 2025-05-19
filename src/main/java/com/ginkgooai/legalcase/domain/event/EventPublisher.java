package com.ginkgooai.legalcase.domain.event;

import com.ginkgooai.legalcase.domain.event.CaseEvents.FormValueRecordedEvent;

/**
 * Interface for publishing domain events
 */
public interface EventPublisher {

	/**
	 * Publish a domain event
	 * @param event the domain event to publish
	 */
	void publish(DomainEvent event);

	/**
	 * Publish a form value recorded event
	 * @param event form value recorded event
	 */
	default void publishEvent(FormValueRecordedEvent event) {
		publish(event);
	}

}