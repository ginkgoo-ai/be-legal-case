package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.dto.QuestionnaireSubmissionRequest;
import com.ginkgooai.legalcase.dto.QuestionnaireSubmissionResponse;

public interface QuestionnaireService {

	/**
	 * Process questionnaire submission
	 * @param request Questionnaire submission request
	 * @return Questionnaire submission response
	 */
	QuestionnaireSubmissionResponse processQuestionnaireSubmission(QuestionnaireSubmissionRequest request);

}