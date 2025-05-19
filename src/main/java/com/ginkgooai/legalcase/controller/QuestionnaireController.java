package com.ginkgooai.legalcase.controller;

import com.ginkgooai.legalcase.dto.QuestionnaireSubmissionRequest;
import com.ginkgooai.legalcase.dto.QuestionnaireSubmissionResponse;
import com.ginkgooai.legalcase.service.QuestionnaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/questionnaires")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Questionnaires", description = "API for managing questionnaire submissions")
public class QuestionnaireController {

	private final QuestionnaireService questionnaireService;

	/**
	 * Receive user information submitted from the questionnaire system
	 * @param request Questionnaire submission request
	 * @return Questionnaire submission response
	 */
	@PostMapping("/submissions")
	@Operation(summary = "Submit questionnaire", description = "Processes a questionnaire submission from a user")
	@ApiResponses(
			value = {
					@ApiResponse(responseCode = "200", description = "Questionnaire submitted successfully",
							content = @Content(
									schema = @Schema(implementation = QuestionnaireSubmissionResponse.class))),
					@ApiResponse(responseCode = "400", description = "Invalid submission data"),
					@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<QuestionnaireSubmissionResponse> submitQuestionnaire(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Questionnaire submission data",
					required = true, content = @Content(schema = @Schema(
							implementation = QuestionnaireSubmissionRequest.class))) @Valid @RequestBody QuestionnaireSubmissionRequest request) {

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
	@PostMapping("/cases/{caseId}/questionnaires")
	@Operation(summary = "Submit case questionnaire",
			description = "Processes a questionnaire submission for a specific case")
	@ApiResponses(
			value = {
					@ApiResponse(responseCode = "200", description = "Questionnaire submitted successfully",
							content = @Content(
									schema = @Schema(implementation = QuestionnaireSubmissionResponse.class))),
					@ApiResponse(responseCode = "400", description = "Invalid submission data"),
					@ApiResponse(responseCode = "404", description = "Case not found"),
					@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<QuestionnaireSubmissionResponse> submitCaseQuestionnaire(
			@Parameter(description = "ID of the case to submit questionnaire for",
					required = true) @PathVariable("caseId") String caseId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Questionnaire submission data",
					required = true, content = @Content(schema = @Schema(
							implementation = QuestionnaireSubmissionRequest.class))) @Valid @RequestBody QuestionnaireSubmissionRequest request) {

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