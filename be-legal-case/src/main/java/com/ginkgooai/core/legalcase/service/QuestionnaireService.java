package com.ginkgooai.core.legalcase.service;

import com.ginkgooai.core.legalcase.dto.QuestionnaireSubmissionRequest;
import com.ginkgooai.core.legalcase.dto.QuestionnaireSubmissionResponse;

public interface QuestionnaireService {

	/**
	 * Process questionnaire submission
	 * @param request Questionnaire submission request
	 * @return Questionnaire submission response
	 */
	QuestionnaireSubmissionResponse processQuestionnaireSubmission(QuestionnaireSubmissionRequest request);

}