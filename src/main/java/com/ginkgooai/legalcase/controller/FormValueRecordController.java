package com.ginkgooai.legalcase.controller;

import com.ginkgooai.legalcase.dto.FormValueRecordDTO;
import com.ginkgooai.legalcase.service.FormValueRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for form value records
 */
@RestController
@RequestMapping("/api/cases/{caseId}/form-records")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Form Value Records", description = "API for managing form value records related to legal cases")
public class FormValueRecordController {

	private final FormValueRecordService formValueRecordService;

	/**
	 * Record form values
	 * @param caseId case ID
	 * @param formValues form values request body
	 * @return recorded form values
	 */
	@PostMapping
	@Operation(summary = "Record form values", description = "Records a set of form values for a specific case")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Form values recorded successfully",
					content = @Content(schema = @Schema(implementation = FormValueRecordDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "404", description = "Case not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<FormValueRecordDTO> recordFormValues(
			@Parameter(description = "ID of the case to record form values for",
					required = true) @PathVariable("caseId") String caseId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Form values to record",
					required = true) @RequestBody Map<String, Object> formValues) {

		String formId = (String) formValues.getOrDefault("formId", "");
		String formName = (String) formValues.getOrDefault("formName", "");
		String pageId = (String) formValues.getOrDefault("pageId", "");
		String pageName = (String) formValues.getOrDefault("pageName", "");
		String userId = (String) formValues.getOrDefault("userId", "anonymous");

		@SuppressWarnings("unchecked")
		Map<String, Object> values = (Map<String, Object>) formValues.getOrDefault("values", Map.of());

		log.info("Recording form values for case: {}, form: {}", caseId, formId);

		FormValueRecordDTO result = formValueRecordService.recordFormValues(caseId, formId, formName, pageId, pageName,
				values, userId);

		return ResponseEntity.ok(result);
	}

	/**
	 * Record a single input value
	 * @param caseId case ID
	 * @param inputData input value request body
	 * @return recorded form values
	 */
	@PostMapping("/inputs")
	@Operation(summary = "Record input value", description = "Records a single input value for a specific case")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Input value recorded successfully",
					content = @Content(schema = @Schema(implementation = FormValueRecordDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "404", description = "Case not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<FormValueRecordDTO> recordInputValue(
			@Parameter(description = "ID of the case to record input value for",
					required = true) @PathVariable("caseId") String caseId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Input data to record",
					required = true) @RequestBody Map<String, Object> inputData) {

		String formId = (String) inputData.getOrDefault("formId", "");
		String formName = (String) inputData.getOrDefault("formName", "");
		String pageId = (String) inputData.getOrDefault("pageId", "");
		String pageName = (String) inputData.getOrDefault("pageName", "");
		String inputId = (String) inputData.getOrDefault("inputId", "");
		String inputType = (String) inputData.getOrDefault("inputType", "");
		String inputValue = String.valueOf(inputData.getOrDefault("inputValue", ""));
		String userId = (String) inputData.getOrDefault("userId", "anonymous");

		log.info("Recording input value for case: {}, form: {}, input: {}", caseId, formId, inputId);

		FormValueRecordDTO result = formValueRecordService.recordInputValue(caseId, formId, formName, pageId, pageName,
				inputId, inputType, inputValue, userId);

		return ResponseEntity.ok(result);
	}

	/**
	 * Get form value records
	 * @param caseId case ID
	 * @param formId form ID (optional)
	 * @return list of form value records
	 */
	@GetMapping
	@Operation(summary = "Get form value records",
			description = "Retrieves form value records for a specific case, optionally filtered by form ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Form value records retrieved successfully",
					content = @Content(
							array = @ArraySchema(schema = @Schema(implementation = FormValueRecordDTO.class)))),
			@ApiResponse(responseCode = "404", description = "Case not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<List<FormValueRecordDTO>> getFormValueRecords(
			@Parameter(description = "ID of the case to get form value records for",
					required = true) @PathVariable("caseId") String caseId,
			@Parameter(description = "ID of the form to filter records by",
					required = false) @RequestParam(value = "formId", required = false) String formId) {

		List<FormValueRecordDTO> records;

		if (formId != null && !formId.isEmpty()) {
			log.info("Getting form value records for case: {} and form: {}", caseId, formId);
			records = formValueRecordService.getFormValueRecords(caseId, formId);
		}
		else {
			log.info("Getting all form value records for case: {}", caseId);
			records = formValueRecordService.getAllFormValueRecords(caseId);
		}

		return ResponseEntity.ok(records);
	}

	/**
	 * Replay form value records
	 * @param caseId case ID
	 * @param formId form ID (optional)
	 * @return replay data
	 */
	@GetMapping("/replay")
	@Operation(summary = "Replay form value records",
			description = "Generates a consolidated view of form value records for replay purposes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Form value records replayed successfully",
					content = @Content(schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "404", description = "Case not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<Map<String, Object>> replayFormValueRecords(
			@Parameter(description = "ID of the case to replay form value records for",
					required = true) @PathVariable("caseId") String caseId,
			@Parameter(description = "ID of the form to filter records by",
					required = false) @RequestParam(value = "formId", required = false) String formId) {

		log.info("Replaying form value records for case: {}, form: {}", caseId, formId);

		Map<String, Object> replayData = formValueRecordService.replayFormValueRecords(caseId, formId);

		return ResponseEntity.ok(replayData);
	}

	/**
	 * Clear form value records
	 * @param caseId case ID
	 * @param formId form ID
	 * @return no content response
	 */
	@DeleteMapping("/{formId}")
	@Operation(summary = "Clear form value records",
			description = "Deletes all form value records for a specific form in a case")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Form value records cleared successfully"),
			@ApiResponse(responseCode = "404", description = "Case or form not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<Void> clearFormValueRecords(
			@Parameter(description = "ID of the case containing the form records",
					required = true) @PathVariable("caseId") String caseId,
			@Parameter(description = "ID of the form to clear records for",
					required = true) @PathVariable("formId") String formId) {

		log.info("Clearing form value records for case: {} and form: {}", caseId, formId);

		formValueRecordService.clearFormValueRecords(caseId, formId);

		return ResponseEntity.noContent().build();
	}
}