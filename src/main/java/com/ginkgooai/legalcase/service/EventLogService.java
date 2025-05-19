package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.dto.EventLogResponse;

import java.util.List;

/**
 * Service for managing event logs
 */
public interface EventLogService {

	/**
	 * Get all event logs for a specific case
	 * @param caseId case ID
	 * @return list of event log responses
	 */
	List<EventLogResponse> getEventLogsForCase(String caseId);

	/**
	 * Get event logs for a specific case and event type
	 * @param caseId case ID
	 * @param eventType event type
	 * @return list of event log responses
	 */
	List<EventLogResponse> getEventLogsForCaseAndType(String caseId, String eventType);

}