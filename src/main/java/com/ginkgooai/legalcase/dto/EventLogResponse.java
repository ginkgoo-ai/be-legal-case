package com.ginkgooai.legalcase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 事件日志响应DTO Event log response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventLogResponse {

	private String id;

	private String caseId;

	private String eventId;

	private String eventType;

	private LocalDateTime occurredAt;

	private String eventData;

	// 表单事件特定字段
	private String formId;

	private String formName;

	private String pageId;

	private String pageName;

	private String inputId;

	private String inputType;

	private String inputValue;

	private Integer sequenceNumber;

	// 创建/更新信息
	private LocalDateTime createdAt;

	private String createdBy;

	private LocalDateTime updatedAt;

	private String updatedBy;

}