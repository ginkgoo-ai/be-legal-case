package com.ginkgooai.legalcase.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base interface for all domain events in the system
 */
public interface DomainEvent {

	String getCaseId();

	/**
	 * Get the unique identifier of this event
	 */
	String getEventId();

	/**
	 * Get the timestamp when this event occurred
	 */
	LocalDateTime getOccurredAt();

	/**
	 * Get the type of this event
	 */
	String getEventType();

}