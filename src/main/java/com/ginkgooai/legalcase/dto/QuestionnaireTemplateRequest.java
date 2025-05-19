package com.ginkgooai.legalcase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for selecting questionnaire templates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireTemplateRequest {

	/**
	 * List of template IDs to use
	 */
	private List<String> templateIds;

}