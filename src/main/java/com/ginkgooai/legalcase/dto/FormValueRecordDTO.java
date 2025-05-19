package com.ginkgooai.legalcase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for form value records
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormValueRecordDTO {

	private String id;

	private String caseId;

	private String formId;

	private String formName;

	private String pageId;

	private String pageName;

	private String inputId;

	private String inputType;

	private String inputValue;

	private Integer sequenceNumber;

	private Map<String, Object> formValues;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime recordedAt;

	private String recordedBy;

	private String eventId;

}