package com.ginkgooai.legalcase.controller;

import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.dto.*;
import com.ginkgooai.legalcase.service.CaseService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for optimized case creation process
 */
@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Legal Case", description = "Legal case management API")
public class CaseController {

	private final CaseService caseService;

	/**
	 * Step 1: Create an empty case
	 * @param request case creation request
	 * @return created case response
	 */
	@PostMapping
	@Operation(summary = "Create a new legal case",
			description = "Creates a new empty legal case with basic information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Case created successfully",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = LegalCaseResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<LegalCaseResponse> createEmptyCase(@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Case creation details", required = true, content = @Content(schema = @Schema(
					implementation = CaseCreationRequest.class))) @RequestBody CaseCreationRequest request) {
		log.info("Received request to create empty case: {}", request.getTitle());

		LegalCase createdCase = caseService.createEmptyCase(request.getTitle(), request.getDescription(),
				request.getProfileId(), request.getClientId());

		return ResponseEntity.ok(caseService.convertToResponse(createdCase));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a legal case by ID", description = "Retrieves a legal case by its unique identifier")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Case found",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = LegalCaseResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "Case not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<LegalCaseResponse> getLegalCase(@Parameter(description = "ID of the legal case to retrieve",
			required = true) @PathVariable("id") String id) {
		LegalCaseResponse response = caseService.getLegalCase(id);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a legal case", description = "Updates an existing legal case with new information")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Case updated successfully",
					content = { @Content(mediaType = "application/json",
							schema = @Schema(implementation = LegalCaseResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "404", description = "Case not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<LegalCaseResponse> updateLegalCase(
			@Parameter(description = "ID of the legal case to update", required = true) @PathVariable("id") String id,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated case details", required = true,
					content = @Content(schema = @Schema(
							implementation = UpdateLegalCaseRequest.class))) @Valid @RequestBody UpdateLegalCaseRequest request) {

		LegalCaseResponse response = caseService.updateLegalCase(id, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a legal case", description = "Deletes a legal case by its unique identifier")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Case deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Case not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<Void> deleteLegalCase(
			@Parameter(description = "ID of the legal case to delete", required = true) @PathVariable("id") String id) {
		caseService.deleteLegalCase(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/search")
	@Operation(summary = "Search legal cases",
			description = "Searches for legal cases based on a search term with pagination")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Search results retrieved",
					content = { @Content(mediaType = "application/json") }),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<Page<LegalCaseResponse>> searchLegalCases(
			@Parameter(description = "Search term", required = true) @RequestParam("q") String searchTerm,
			@Parameter(description = "Pagination and sorting parameters") @PageableDefault(sort = "createdAt",
					direction = Sort.Direction.DESC) Pageable pageable) {

		Page<LegalCaseResponse> response = caseService.searchLegalCases(searchTerm, pageable);
		return ResponseEntity.ok(response);
	}

}