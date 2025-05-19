package com.ginkgooai.legalcase.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base abstract class for all domain events
 */
@Getter
public abstract class BaseDomainEvent implements DomainEvent {

	private final String caseId;

	private final String eventId;

	private final LocalDateTime occurredAt;

	protected BaseDomainEvent(String caseId) {
		this.caseId = caseId;
		this.eventId = UUID.randomUUID().toString();
		this.occurredAt = LocalDateTime.now();
	}

	@Override
	public String getEventType() {
		return this.getClass().getSimpleName();
	}

}