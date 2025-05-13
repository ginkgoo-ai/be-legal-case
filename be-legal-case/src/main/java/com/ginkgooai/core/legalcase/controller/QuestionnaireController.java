package com.ginkgooai.core.legalcase.controller;

import com.ginkgooai.core.legalcase.dto.QuestionnaireSubmissionRequest;
import com.ginkgooai.core.legalcase.dto.QuestionnaireSubmissionResponse;
import com.ginkgooai.core.legalcase.service.QuestionnaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questionnaires")
@RequiredArgsConstructor
@Slf4j
public class QuestionnaireController {

	private final QuestionnaireService questionnaireService;

	/**
	 * Receive user information submitted from the questionnaire system
	 * @param request Questionnaire submission request
	 * @return Questionnaire submission response
	 */
	@PostMapping("/submissions")
	public ResponseEntity<QuestionnaireSubmissionResponse> submitQuestionnaire(
			@Valid @RequestBody QuestionnaireSubmissionRequest request) {

		log.info("Received questionnaire submission: userId={}, questionnaireId={}", request.getUserId(),
				request.getQuestionnaireId());

		QuestionnaireSubmissionResponse response = questionnaireService.processQuestionnaireSubmission(request);

		if ("COMPLETED".equals(response.getStatus())) {
			return ResponseEntity.ok(response);
		}
		else {
			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * Receive questionnaire submission for a specific case
	 * @param caseId Case ID
	 * @param request Questionnaire submission request
	 * @return Questionnaire submission response
	 */
	@PostMapping("/cases/{caseId}/submissions")
	public ResponseEntity<QuestionnaireSubmissionResponse> submitCaseQuestionnaire(
			@PathVariable("caseId") String caseId, @Valid @RequestBody QuestionnaireSubmissionRequest request) {

		log.info("Received case questionnaire submission: caseId={}, userId={}, questionnaireId={}", caseId,
				request.getUserId(), request.getQuestionnaireId());

		// Set case ID
		request.setCaseId(caseId);

		QuestionnaireSubmissionResponse response = questionnaireService.processQuestionnaireSubmission(request);

		if ("COMPLETED".equals(response.getStatus())) {
			return ResponseEntity.ok(response);
		}
		else {
			return ResponseEntity.badRequest().body(response);
		}
	}

}