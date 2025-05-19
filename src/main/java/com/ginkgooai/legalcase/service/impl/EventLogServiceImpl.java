package com.ginkgooai.legalcase.service.impl;

import com.ginkgooai.legalcase.domain.event.EventLog;
import com.ginkgooai.legalcase.dto.EventLogResponse;
import com.ginkgooai.legalcase.exception.ResourceNotFoundException;
import com.ginkgooai.legalcase.repository.EventLogRepository;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 事件日志服务实现 Event log service implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventLogServiceImpl implements EventLogService {

	private final EventLogRepository eventLogRepository;

	private final LegalCaseRepository legalCaseRepository;

	/**
	 * 获取特定案例的所有事件日志 Get all event logs for a specific case
	 * @param caseId 案例ID / case ID
	 * @return 事件日志响应列表 / list of event log responses
	 */
	@Override
	@Transactional(readOnly = true)
	public List<EventLogResponse> getEventLogsForCase(String caseId) {
		log.info("Getting event logs for case: {}", caseId);

		// 验证案例是否存在
		if (!legalCaseRepository.existsById(caseId)) {
			throw new ResourceNotFoundException("Case not found: " + caseId);
		}

		// 获取所有事件日志
		List<EventLog> eventLogs = eventLogRepository.findByLegalCaseIdOrderByOccurredAtAsc(caseId);

		// 转换为响应DTO
		return eventLogs.stream().map(this::convertToResponse).collect(Collectors.toList());
	}

	/**
	 * 获取特定案例的特定类型事件日志 Get event logs for a specific case and event type
	 * @param caseId 案例ID / case ID
	 * @param eventType 事件类型 / event type
	 * @return 事件日志响应列表 / list of event log responses
	 */
	@Override
	@Transactional(readOnly = true)
	public List<EventLogResponse> getEventLogsForCaseAndType(String caseId, String eventType) {
		log.info("Getting event logs for case: {} and type: {}", caseId, eventType);

		// 验证案例是否存在
		if (!legalCaseRepository.existsById(caseId)) {
			throw new ResourceNotFoundException("Case not found: " + caseId);
		}

		// 获取特定类型的事件日志
		List<EventLog> eventLogs = eventLogRepository.findByLegalCaseIdAndEventTypeOrderByOccurredAtAsc(caseId,
				eventType);

		// 转换为响应DTO
		return eventLogs.stream().map(this::convertToResponse).collect(Collectors.toList());
	}

	/**
	 * 将事件日志实体转换为响应DTO Convert event log entity to response DTO
	 * @param eventLog 事件日志实体 / event log entity
	 * @return 事件日志响应 / event log response
	 */
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