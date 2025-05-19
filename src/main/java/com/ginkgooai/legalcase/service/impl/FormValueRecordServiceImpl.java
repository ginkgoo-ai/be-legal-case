package com.ginkgooai.legalcase.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.legalcase.domain.CaseStatus;
import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.domain.event.CaseEvents;
import com.ginkgooai.legalcase.domain.event.DomainEvent;
import com.ginkgooai.legalcase.domain.event.EventLog;
import com.ginkgooai.legalcase.domain.event.EventPublisher;
import com.ginkgooai.legalcase.dto.FormValueRecordDTO;
import com.ginkgooai.legalcase.exception.FormValueRecordingException;
import com.ginkgooai.legalcase.repository.EventLogRepository;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.FormValueRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表单值记录服务实现 Service implementation for form value records
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FormValueRecordServiceImpl implements FormValueRecordService {

	private final LegalCaseRepository legalCaseRepository;

	private final EventLogRepository eventLogRepository;

	private final EventPublisher eventPublisher;

	private final ObjectMapper objectMapper;

	private static final String FORM_VALUE_EVENT_TYPE = "FormValueRecorded";

	/**
	 * 记录表单值 Record form values
	 */
	@Override
	@Transactional
	public FormValueRecordDTO recordFormValues(String caseId, String formId, String formName, String pageId,
			String pageName, Map<String, Object> formValues, String userId) {

		log.info("Recording form values for case: {}, form: {}, page: {}", caseId, formName, pageName);

		// 查找案例
		LegalCase legalCase = legalCaseRepository.findById(caseId)
			.orElseThrow(() -> new ResourceNotFoundException("Legal case", "caseId", caseId));

		// 使用LegalCase领域方法记录表单值
		Map<String, Object> valueMap = new HashMap<>(formValues);
		for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
			String inputId = entry.getKey();
			String inputValue = String.valueOf(entry.getValue());
			legalCase.recordFormValue(formId, formName, pageId, pageName, inputId, "unknown", inputValue);
		}

		// 检查是否需要开始自动填充（如果案例状态为 DOCUMENTATION_COMPLETE 或 READY_TO_FILL）
		if (legalCase.getStatus() == CaseStatus.DOCUMENTATION_COMPLETE
				|| legalCase.getStatus() == CaseStatus.READY_TO_FILL) {
			legalCase.initiateAutoFilling();
		}

		// 保存LegalCase以触发事件
		legalCaseRepository.save(legalCase);

		// 处理领域事件并返回FormValueRecordDTO
		List<DomainEvent> events = legalCase.getAndClearDomainEvents();
		for (DomainEvent event : events) {
			if (event instanceof CaseEvents.FormValueRecordedEvent) {
				CaseEvents.FormValueRecordedEvent formEvent = (CaseEvents.FormValueRecordedEvent) event;

				// 发布事件到EventPublisher
				eventPublisher.publishEvent(formEvent);

				// 构建返回值
				FormValueRecordDTO dto = FormValueRecordDTO.builder()
					.caseId(caseId)
					.formId(formId)
					.formName(formName)
					.pageId(pageId)
					.pageName(pageName)
					.formValues(formValues)
					.recordedAt(LocalDateTime.now())
					.recordedBy(userId)
					.eventId(formEvent.getEventId())
					.build();

				return dto;
			}
		}

		throw new FormValueRecordingException("Failed to record form values: no event was generated");
	}

	@Override
	@Transactional
	public FormValueRecordDTO recordInputValue(String caseId, String formId, String formName, String pageId,
			String pageName, String inputId, String inputType, String inputValue, String userId) {

		log.info("Recording input value for case: {}, form: {}, page: {}, input: {}", caseId, formName, pageName,
				inputId);

		LegalCase legalCase = legalCaseRepository.findById(caseId)
			.orElseThrow(() -> new ResourceNotFoundException("Legal case", "caseId", caseId));

		// 使用LegalCase领域方法记录单个输入值
		legalCase.recordFormValue(formId, formName, pageId, pageName, inputId, inputType, inputValue);

		// 检查是否需要开始自动填充（如果案例状态为 DOCUMENTATION_COMPLETE 或 READY_TO_FILL）
		if (legalCase.getStatus() == CaseStatus.DOCUMENTATION_COMPLETE
				|| legalCase.getStatus() == CaseStatus.READY_TO_FILL) {
			legalCase.initiateAutoFilling();
		}

		// 保存LegalCase以触发事件
		legalCaseRepository.save(legalCase);

		// 处理领域事件并返回FormValueRecordDTO
		List<DomainEvent> events = legalCase.getAndClearDomainEvents();
		for (DomainEvent event : events) {
			if (event instanceof CaseEvents.FormValueRecordedEvent) {
				CaseEvents.FormValueRecordedEvent formEvent = (CaseEvents.FormValueRecordedEvent) event;

				// 发布事件到EventPublisher
				eventPublisher.publishEvent(formEvent);

				// 构建返回值
				Map<String, Object> formValues = new HashMap<>();
				formValues.put(inputId, inputValue);

				FormValueRecordDTO dto = FormValueRecordDTO.builder()
					.caseId(caseId)
					.formId(formId)
					.formName(formName)
					.pageId(pageId)
					.pageName(pageName)
					.inputId(inputId)
					.inputType(inputType)
					.inputValue(inputValue)
					.formValues(formValues)
					.recordedAt(LocalDateTime.now())
					.recordedBy(userId)
					.eventId(formEvent.getEventId())
					.build();

				return dto;
			}
		}

		throw new FormValueRecordingException("Failed to record input value: no event was generated");
	}

	@Override
	@Transactional(readOnly = true)
	public List<FormValueRecordDTO> getAllFormValueRecords(String caseId) {
		log.info("Getting all form value records for case: {}", caseId);

		if (!legalCaseRepository.existsById(caseId)) {
			throw new ResourceNotFoundException("Legal case", "caseId", caseId);
		}

		List<EventLog> eventLogs = eventLogRepository.findByLegalCaseIdAndEventTypeOrderByOccurredAtAsc(caseId,
				FORM_VALUE_EVENT_TYPE);

		return eventLogs.stream().map(this::convertEventLogToDTO).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<FormValueRecordDTO> getFormValueRecords(String caseId, String formId) {
		log.info("Getting form value records for case: {} and form: {}", caseId, formId);

		if (!legalCaseRepository.existsById(caseId)) {
			throw new ResourceNotFoundException("Legal case", "caseId", caseId);
		}

		List<EventLog> eventLogs = eventLogRepository.findByLegalCaseIdOrderByOccurredAtAsc(caseId);

		return eventLogs.stream().map(this::convertEventLogToDTO).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void clearFormValueRecords(String caseId, String formId) {
		log.info("Clearing form value records for case: {} and form: {}", caseId, formId);

		if (!legalCaseRepository.existsById(caseId)) {
			throw new ResourceNotFoundException("Legal case", "caseId", caseId);

		}

		log.info("Form value records cannot be physically deleted as they are part of event history.");
		log.info("However, they can be ignored for future replays.");
	}

	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> replayFormValueRecords(String caseId, String formId) {
		log.info("Replaying form value records for case: {}, form: {}", caseId, formId);

		if (!legalCaseRepository.existsById(caseId)) {
			throw new ResourceNotFoundException("Legal case", "caseId", caseId);
		}

		List<EventLog> eventLogs = eventLogRepository.findByLegalCaseIdOrderByOccurredAtAsc(caseId);

		List<FormValueRecordDTO> records = eventLogs.stream()
			.map(this::convertEventLogToDTO)
			.collect(Collectors.toList());

		Map<String, Object> replayData = new HashMap<>();

		Map<String, Map<String, List<FormValueRecordDTO>>> formPageRecords = records.stream()
			.collect(Collectors.groupingBy(FormValueRecordDTO::getFormId,
					Collectors.groupingBy(FormValueRecordDTO::getPageId)));

		replayData.put("forms", formPageRecords);

		Map<String, Map<String, String>> inputValues = new HashMap<>();
		records.stream().filter(r -> r.getInputId() != null).forEach(r -> {
			String formKey = r.getFormId();
			inputValues.computeIfAbsent(formKey, k -> new HashMap<>()).put(r.getInputId(), r.getInputValue());
		});

		replayData.put("inputs", inputValues);

		return replayData;
	}

	private FormValueRecordDTO convertEventLogToDTO(EventLog eventLog) {
		try {
			CaseEvents.FormValueRecordedEvent event = objectMapper.readValue(eventLog.getEventData(),
					CaseEvents.FormValueRecordedEvent.class);

			return FormValueRecordDTO.builder()
				.id(eventLog.getId())
				.caseId(eventLog.getLegalCase().getId())
				.formValues(event.getFormValues())
				.recordedAt(eventLog.getOccurredAt())
				.recordedBy(eventLog.getCreatedBy())
				.eventId(eventLog.getEventId())
				.build();
		}
		catch (JsonProcessingException e) {
			log.error("Error deserializing event data", e);

			return FormValueRecordDTO.builder()
				.id(eventLog.getId())
				.caseId(eventLog.getLegalCase().getId())
				.recordedAt(eventLog.getOccurredAt())
				.recordedBy(eventLog.getCreatedBy())
				.eventId(eventLog.getEventId())
				.build();
		}
	}

}