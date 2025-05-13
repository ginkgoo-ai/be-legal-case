package com.ginkgooai.core.legalcase.controller;

import com.ginkgooai.core.legalcase.dto.CaseDocumentResponse;
import com.ginkgooai.core.legalcase.dto.CreateCaseDocumentRequest;
import com.ginkgooai.core.legalcase.service.CaseDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases/{caseId}/documents")
@RequiredArgsConstructor
public class CaseDocumentController {

	private final CaseDocumentService caseDocumentService;

	/**
	 * 为案件上传文档
	 */
	@PostMapping(consumes = { "multipart/form-data" })
	public ResponseEntity<CaseDocumentResponse> uploadDocument(@PathVariable("caseId") UUID caseId,
			@RequestParam("file") MultipartFile file, @RequestParam("title") String title,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "documentType", required = false) String documentType) {

		CreateCaseDocumentRequest request = CreateCaseDocumentRequest.builder()
			.title(title)
			.description(description)
			.documentType(documentType)
			.build();

		CaseDocumentResponse response = caseDocumentService.uploadDocument(caseId, file, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 获取案件的所有文档
	 */
	@GetMapping
	public ResponseEntity<Page<CaseDocumentResponse>> getDocuments(@PathVariable("caseId") UUID caseId,
			@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		Page<CaseDocumentResponse> response = caseDocumentService.getDocumentsByCaseId(caseId, pageable);
		return ResponseEntity.ok(response);
	}

	/**
	 * 获取文档详情
	 */
	@GetMapping("/{documentId}")
	public ResponseEntity<CaseDocumentResponse> getDocument(@PathVariable("caseId") UUID caseId,
			@PathVariable("documentId") UUID documentId) {

		CaseDocumentResponse response = caseDocumentService.getDocument(caseId, documentId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 更新文档信息
	 */
	@PutMapping("/{documentId}")
	public ResponseEntity<CaseDocumentResponse> updateDocument(@PathVariable("caseId") UUID caseId,
			@PathVariable("documentId") UUID documentId, @Valid @RequestBody CreateCaseDocumentRequest request) {

		CaseDocumentResponse response = caseDocumentService.updateDocument(caseId, documentId, request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 删除文档
	 */
	@DeleteMapping("/{documentId}")
	public ResponseEntity<Void> deleteDocument(@PathVariable("caseId") UUID caseId,
			@PathVariable("documentId") UUID documentId) {

		caseDocumentService.deleteDocument(caseId, documentId);
		return ResponseEntity.noContent().build();
	}

}