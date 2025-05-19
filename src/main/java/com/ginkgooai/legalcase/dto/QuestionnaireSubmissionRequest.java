package com.ginkgooai.legalcase.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireSubmissionRequest {

	@NotNull(message = "Questionnaire ID cannot be empty")
	private String questionnaireId;

	@NotNull(message = "User ID cannot be empty")
	private String userId;

	private String caseId;

	private String questionnaireType;

	@NotNull(message = "Questionnaire responses cannot be empty")
	private Map<String, Object> responses;

}