package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.domain.event.DomainEvent;
import com.ginkgooai.legalcase.dto.EventLogResponse;
import com.ginkgooai.legalcase.dto.LegalCaseResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Case event SSE emitter service interface
 */
public interface CaseEventEmitterService {

	/**
	 * Add SSE emitter for a specific case
	 * @param caseId Case ID
	 * @param emitter SSE emitter
	 * @return Added emitter
	 */
	SseEmitter addEmitter(String caseId, SseEmitter emitter);

	/**
	 * Remove SSE emitter for a specific case
	 * @param caseId Case ID
	 * @param emitter SSE emitter
	 */
	void removeEmitter(String caseId, SseEmitter emitter);

	/**
	 * Send case update to all SSE emitters for a specific case
	 * @param caseId Case ID
	 * @param caseResponse Latest case response
	 */
	void sendCaseUpdate(String caseId, LegalCaseResponse caseResponse);

	/**
	 * Send event update to all SSE emitters for a specific case
	 * @param caseId Case ID
	 * @param eventResponse Event response
	 */
	void sendEventUpdate(String caseId, EventLogResponse eventResponse);

	/**
	 * Handle new domain event and send appropriate SSE messages
	 * @param event Domain event
	 */
	void handleDomainEvent(DomainEvent event);

}