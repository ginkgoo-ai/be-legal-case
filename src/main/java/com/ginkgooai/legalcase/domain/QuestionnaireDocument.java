package com.ginkgooai.legalcase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Questionnaire document type
 */
@Entity
@DiscriminatorValue("QUESTIONNAIRE")
@Getter
@Setter
@NoArgsConstructor
public class QuestionnaireDocument extends CaseDocument {

	@Column(name = "questionnaire_type")
	private String questionnaireType;

	@Column(name = "completion_percentage")
	private Integer completionPercentage;

	@Transient
	private Map<String, Object> responses = new HashMap<>();

	/**
	 * Check if this specific questionnaire is complete
	 */
	@Override
	public boolean isComplete() {
		return getStatus() == DocumentStatus.COMPLETE || (completionPercentage != null && completionPercentage >= 100);
	}

}