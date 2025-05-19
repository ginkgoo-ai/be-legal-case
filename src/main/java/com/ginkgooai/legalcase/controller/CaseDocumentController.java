package com.ginkgooai.legalcase.controller;

import com.ginkgooai.legalcase.domain.CaseDocument;
import com.ginkgooai.legalcase.dto.BatchDocumentUploadRequest;
import com.ginkgooai.legalcase.dto.DocumentStatusResponse;
import com.ginkgooai.legalcase.service.CaseDocumentService;
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
import java.util.stream.Collectors;

/**
 * Controller for document upload operations
 */
@RestController
@RequestMapping("/cases/{caseId}/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Case Documents", description = "API for managing documents related to legal cases")
public class CaseDocumentController {

	private final CaseDocumentService documentUploadService;

	/**
	 * Upload multiple documents for a case
	 * @param caseId case ID
	 * @param request batch document upload request containing list of storage IDs
	 * @return list of created document IDs
	 */
	@PostMapping()
	@Operation(summary = "Upload multiple documents",
			description = "Upload multiple documents for a case using storage IDs")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Documents uploaded successfully",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "404", description = "Case not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<List<String>> uploadDocuments(
			@Parameter(description = "ID of the case to upload documents to",
					required = true) @PathVariable String caseId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of storage IDs for documents",
					required = true, content = @Content(schema = @Schema(
							implementation = BatchDocumentUploadRequest.class))) @RequestBody BatchDocumentUploadRequest request) {
		log.info("Received request to upload {} documents for case: {}", request.getStorageIds().size(), caseId);

		List<String> documentIds = documentUploadService.uploadDocuments(caseId, request.getStorageIds());

		return ResponseEntity.ok(documentIds);
	}

	/**
	 * Get status of a document
	 * @param caseId case ID
	 * @param documentId document ID
	 * @return document status response
	 */
	@GetMapping("/{documentId}/status")
	@Operation(summary = "Get document status", description = "Get the status of a specific document in a case")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Document status retrieved successfully",
					content = @Content(schema = @Schema(implementation = DocumentStatusResponse.class))),
			@ApiResponse(responseCode = "404", description = "Document or case not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<DocumentStatusResponse> getDocumentStatus(
			@Parameter(description = "ID of the case", required = true) @PathVariable String caseId,
			@Parameter(description = "ID of the document to get status for",
					required = true) @PathVariable String documentId) {
		log.info("Received request to get status for document: {} in case: {}", documentId, caseId);

		CaseDocument document = documentUploadService.getDocument(documentId)
			.orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

		DocumentStatusResponse response = DocumentStatusResponse.builder()
			.documentId(document.getId())
			.status(document.getStatus().name())
			.documentType(document.getDocumentType() != null ? document.getDocumentType().name() : null)
			.documentCategory(document.getDocumentCategory().name())
			.isComplete(document.isComplete())
			.build();

		return ResponseEntity.ok(response);
	}

	/**
	 * Get all documents for a case
	 * @param caseId case ID
	 * @return list of document status responses
	 */
	@GetMapping
	@Operation(summary = "Get all case documents",
			description = "Retrieve all documents associated with a specific case")
	@ApiResponses(
			value = {
					@ApiResponse(responseCode = "200", description = "Documents retrieved successfully",
							content = @Content(array = @ArraySchema(
									schema = @Schema(implementation = DocumentStatusResponse.class)))),
					@ApiResponse(responseCode = "404", description = "Case not found"),
					@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<List<DocumentStatusResponse>> getAllDocuments(@Parameter(
			description = "ID of the case to get documents for", required = true) @PathVariable String caseId) {
		log.info("Received request to get all documents for case: {}", caseId);

		List<CaseDocument> documents = documentUploadService.getDocumentsByCaseId(caseId);

		List<DocumentStatusResponse> responses = documents.stream()
			.map(document -> DocumentStatusResponse.builder()
				.documentId(document.getId())
				.status(document.getStatus().name())
				.documentType(document.getDocumentType() != null ? document.getDocumentType().name() : null)
				.documentCategory(document.getDocumentCategory().name())
				.isComplete(document.isComplete())
				.title(document.getTitle())
				.build())
			.collect(Collectors.toList());

		return ResponseEntity.ok(responses);
	}

}