package com.ginkgooai.legalcase.service.impl;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.legalcase.domain.event.EventLog;
import com.ginkgooai.legalcase.dto.EventLogResponse;
import com.ginkgooai.legalcase.repository.EventLogRepository;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventLogServiceImpl implements EventLogService {

	private final EventLogRepository eventLogRepository;

	private final LegalCaseRepository legalCaseRepository;

	@Override
	@Transactional(readOnly = true)
	public List<EventLogResponse> getEventLogsForCase(String caseId) {
		log.info("Getting event logs for case: {}", caseId);

		if (!legalCaseRepository.existsById(caseId)) {
			throw new ResourceNotFoundException("Legal case", "caseId", caseId);
		}

		List<EventLog> eventLogs = eventLogRepository.findByLegalCaseIdOrderByOccurredAtAsc(caseId);

		return eventLogs.stream().map(this::convertToResponse).collect(Collectors.toList());
	}


	@Override
	@Transactional(readOnly = true)
	public List<EventLogResponse> getEventLogsForCaseAndType(String caseId, String eventType) {
		log.info("Getting event logs for case: {} and type: {}", caseId, eventType);

		if (!legalCaseRepository.existsById(caseId)) {
			throw new ResourceNotFoundException("Legal case", "caseId", caseId);
		}

		List<EventLog> eventLogs = eventLogRepository.findByLegalCaseIdAndEventTypeOrderByOccurredAtAsc(caseId,
				eventType);

		return eventLogs.stream().map(this::convertToResponse).collect(Collectors.toList());
	}

	private EventLogResponse convertToResponse(EventLog eventLog) {
		return EventLogResponse.builder()
			.id(eventLog.getId())
			.caseId(eventLog.getLegalCase().getId())
			.eventId(eventLog.getEventId())
			.eventType(eventLog.getEventType())
			.occurredAt(eventLog.getOccurredAt())
			.eventData(eventLog.getEventData())
			.createdAt(eventLog.getCreatedAt())
			.createdBy(eventLog.getCreatedBy())
			.updatedAt(eventLog.getUpdatedAt())
			.updatedBy(eventLog.getUpdatedBy())
			.build();
	}

}