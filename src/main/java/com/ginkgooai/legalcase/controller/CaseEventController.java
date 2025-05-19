package com.ginkgooai.legalcase.controller;

import com.ginkgooai.legalcase.dto.EventLogResponse;
import com.ginkgooai.legalcase.service.EventLogService;
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

/**
 * Case events controller
 */
@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Case Events", description = "API for retrieving events related to legal cases")
public class CaseEventController {

	private final EventLogService eventLogService;

	/**
	 * Get all events for a specific case
	 * @param caseId case ID
	 * @return list of events
	 */
	@GetMapping("/{caseId}/events")
	@Operation(summary = "Get all case events", description = "Retrieves all events associated with a specific case")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Events retrieved successfully",
					content = @Content(
							array = @ArraySchema(schema = @Schema(implementation = EventLogResponse.class)))),
			@ApiResponse(responseCode = "404", description = "Case not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<List<EventLogResponse>> getCaseEvents(
			@Parameter(description = "ID of the case to get events for", required = true) @PathVariable String caseId) {
		log.info("Received request to get events for case: {}", caseId);

		List<EventLogResponse> events = eventLogService.getEventLogsForCase(caseId);
		return ResponseEntity.ok(events);
	}

	/**
	 * Get events of a specific type for a case
	 * @param caseId case ID
	 * @param eventType event type
	 * @return list of events
	 */
	@GetMapping("/{caseId}/events/types/{eventType}")
	@Operation(summary = "Get case events by type",
			description = "Retrieves events of a specific type associated with a case")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Events retrieved successfully",
					content = @Content(
							array = @ArraySchema(schema = @Schema(implementation = EventLogResponse.class)))),
			@ApiResponse(responseCode = "404", description = "Case or event type not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<List<EventLogResponse>> getCaseEventsByType(
			@Parameter(description = "ID of the case to get events for", required = true) @PathVariable String caseId,
			@Parameter(description = "Type of events to retrieve", required = true) @PathVariable String eventType) {
		log.info("Received request to get events of type: {} for case: {}", eventType, caseId);

		List<EventLogResponse> events = eventLogService.getEventLogsForCaseAndType(caseId, eventType);
		return ResponseEntity.ok(events);
	}

}