package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.client.storage.StorageClient;
import com.ginkgooai.legalcase.client.storage.dto.CloudFileResponse;
import com.ginkgooai.legalcase.domain.*;
import com.ginkgooai.legalcase.repository.CaseDocumentRepository;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.ai.DocumentAnalysisService;
import com.ginkgooai.legalcase.service.event.DomainEventPublisherFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service for document upload and processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaseDocumentService {

	private final LegalCaseRepository legalCaseRepository;

	private final CaseDocumentRepository caseDocumentRepository;

	private final StorageClient storageClient;

	private final DocumentAnalysisService documentAnalysisService;

	private final DomainEventPublisherFactory eventPublisherFactory;

	/**
	 * Get a document by ID
	 * @param documentId document ID
	 * @return optional containing the document if found
	 */
	@Transactional(readOnly = true)
	public Optional<CaseDocument> getDocument(String documentId) {
		return caseDocumentRepository.findById(documentId);
	}

	/**
	 * Get all documents for a case
	 * @param caseId case ID
	 * @return list of documents
	 */
	@Transactional(readOnly = true)
	public List<CaseDocument> getDocumentsByCaseId(String caseId) {
		return legalCaseRepository.findDocumentsByCaseId(caseId);
	}

	/**
	 * Upload multiple documents for a case
	 * @param caseId case ID
	 * @param storageIds list of storage IDs for uploaded files
	 * @return list of created document IDs
	 */
	@Transactional
	public List<String> uploadDocuments(String caseId, List<String> storageIds) {
		log.info("Uploading {} documents for case: {}", storageIds.size(), caseId);

		LegalCase legalCase = legalCaseRepository.findById(caseId)
			.orElseThrow(() -> new EntityNotFoundException("Case not found: " + caseId));

		List<CaseDocument> createdDocuments = new ArrayList<>();

		ResponseEntity<List<CloudFileResponse>> response = storageClient.getFileDetails(storageIds);

		if (response.getStatusCode().isError() || response.getBody() == null) {
			throw new RuntimeException("Failed to get file details: " + response.getStatusCode());
		}

		List<CloudFileResponse> fileResponses = response.getBody();

		Map<String, CloudFileResponse> fileInfoMap = fileResponses.stream()
			.collect(Collectors.toMap(CloudFileResponse::getId, file -> file));

		// 先删除已存在的相同storageId的文档
		for (String storageId : storageIds) {
			// 查找case下具有相同storageId的文档
			List<CaseDocument> existingDocuments = caseDocumentRepository.findByCaseIdAndStorageId(caseId, storageId);

			if (!existingDocuments.isEmpty()) {
				log.info("Removing {} existing documents with storageId {} from case {}", existingDocuments.size(),
						storageId, caseId);

				// 从case的文档集合中移除
				for (CaseDocument existingDoc : existingDocuments) {
					legalCase.getDocuments().remove(existingDoc);
				}

				// 从数据库中删除
				caseDocumentRepository.deleteAll(existingDocuments);
			}
		}

		// Process each storage ID and create documents in PENDING status
		for (String storageId : storageIds) {
			CloudFileResponse fileInfo = fileInfoMap.get(storageId);

			if (fileInfo == null) {
				log.warn("File info not found for storage ID: {}", storageId);
				continue;
			}

			String fileName = fileInfo.getOriginalName();
			String fileType = fileInfo.getFileType();
			Long fileSize = fileInfo.getFileSize();
			String publicUrl = storageClient.generatePresignedUrl(fileInfo.getStorageName())
				.getBody()
				.getFile()
				.toString();

			// Create a generic document first, type will be determined by AI
			CaseDocument document = new CaseDocument();
			document.setTitle(fileName);
			document.setDescription("Uploaded document awaiting analysis");
			document.setFilePath(publicUrl);
			document.setFileType(fileType);
			document.setFileSize(fileSize);
			document.setStorageId(storageId);
			document.setStatus(CaseDocument.DocumentStatus.PENDING);
			document.setDocumentCategory(CaseDocument.DocumentCategory.SUPPORTING_DOCUMENT); // Default
																								// category
			document.setLegalCase(legalCase);

			legalCase.getDocuments().add(document);
			createdDocuments.add(document);
		}

		if (!createdDocuments.isEmpty()) {
			legalCase.initiateLlmAnalysis("document_analysis");
		}

		// 统一保存所有文档
		LegalCase savedLegalCase = legalCaseRepository.save(legalCase);
		eventPublisherFactory.publishEvents(legalCase);

		log.info("Created {} documents for case: {}", createdDocuments.size(), caseId);

		// 使用TransactionSynchronizationManager在事务提交后执行异步操作
		final List<CaseDocument> pendingDocuments = savedLegalCase.getDocuments()
			.stream()
			.filter(doc -> doc.getStatus() == CaseDocument.DocumentStatus.PENDING)
			.collect(Collectors.toList());

		org.springframework.transaction.support.TransactionSynchronizationManager
			.registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
				@Override
				public void afterCommit() {
					for (CaseDocument document : pendingDocuments) {
						String docId = document.getId();
						String publicUrl = document.getFilePath();
						CompletableFuture.runAsync(() -> {
							queueDocumentForAnalysis(caseId, docId, publicUrl);
						});
					}
				}
			});

		return pendingDocuments.stream().map(CaseDocument::getId).collect(Collectors.toList());
	}

	/**
	 * Queue a document for AI analysis to determine its type and category
	 * @param documentId document ID
	 * @param publicUrl public URL to access the document
	 */
	@Async
	public void queueDocumentForAnalysis(String caseId, String documentId, String publicUrl) {
		log.info("Queuing document for analysis: {}", documentId);

		try {
			// Call the document analysis service directly from this async method
			documentAnalysisService.analyzeDocument(caseId, documentId, publicUrl, this::updateDocumentAfterAnalysis);
		}
		catch (Exception e) {
			log.error("Error queuing document for analysis: {}", documentId, e);
			updateDocumentWithError(documentId, "Error queuing for analysis: " + e.getMessage());
		}
	}

	/**
	 * Update document after AI analysis
	 * @param analysisResult the analysis result containing document type, category, and
	 * extracted data
	 */
	@Transactional
	public void updateDocumentAfterAnalysis(Pair pair, Map<String, Object> analysisResult) {

		String caseId = pair.getLeft().toString();
		String documentId = pair.getRight().toString();
		log.info("Updating document with analysis results: {}", pair.getRight());
		try {
			// Find the document with its legal case eagerly loaded
			LegalCase legalCase = legalCaseRepository.findByIdWithDocuments(caseId)
				.orElseThrow(() -> new EntityNotFoundException("Legal case not found: " + caseId));
			CaseDocument document = legalCase.getDocuments()
				.stream()
				.filter(doc -> doc.getId().equals(documentId))
				.findFirst()
				.orElse(null);

			if (document == null) {
				log.error("Original document with ID {} not found in case {}", documentId, caseId);
				// Optionally throw an exception or handle as an error
				updateDocumentWithError(documentId, "Original document not found for update after analysis.");
				return;
			}

			// Get analysis results
			String detectedType = (String) analysisResult.get("documentType");
			String detectedCategory = (String) analysisResult.get("documentCategory");
			Map<String, Object> extractedData = (Map<String, Object>) analysisResult.get("extractedData");
			boolean isComplete = (boolean) analysisResult.getOrDefault("isComplete", false);

			// Create the appropriate document type based on category
			CaseDocument typedDocument;
			switch (CaseDocument.DocumentCategory.valueOf(detectedCategory)) {
				case QUESTIONNAIRE:
					typedDocument = new QuestionnaireDocument();
					BeanUtils.copyProperties(document, typedDocument); // Copies ID as
																		// well

					((QuestionnaireDocument) typedDocument).setQuestionnaireType(detectedType);
					((QuestionnaireDocument) typedDocument).setResponsesJson(convertMapToJson(extractedData));
					((QuestionnaireDocument) typedDocument).setCompletionPercentage(isComplete ? 100 : 50);
					break;

				case PROFILE:
					typedDocument = new ProfileDocument();
					BeanUtils.copyProperties(document, typedDocument); // Copies ID as
																		// well

					((ProfileDocument) typedDocument).setProfileType(detectedType);
					((ProfileDocument) typedDocument).setIdentityVerified(isComplete);
					break;

				case SUPPORTING_DOCUMENT:
					typedDocument = new SupportingDocument();
					BeanUtils.copyProperties(document, typedDocument); // Copies ID as
																		// well

					((SupportingDocument) typedDocument).setDocumentReference(UUID.randomUUID().toString());
					((SupportingDocument) typedDocument).setIssueDate(extractedData.get("issueDate") != null
							? java.time.LocalDateTime.parse((String) extractedData.get("issueDate")) : null);
					((SupportingDocument) typedDocument).setExpiryDate(extractedData.get("expiryDate") != null
							? java.time.LocalDateTime.parse((String) extractedData.get("expiryDate")) : null);
					((SupportingDocument) typedDocument).setVerificationRequired(true);
					((SupportingDocument) typedDocument).setVerified(isComplete);

					break;

				default:
					throw new IllegalArgumentException("Unsupported document category: " + detectedCategory);
			}

			// Remove the old document and add the new typed document to the case's
			// collection
			// This is crucial for JPA to correctly manage the relationship and persist
			// the typed document
			// legalCase.getDocuments().remove(document);
			// legalCase.getDocuments().add(typedDocument);

			// Set common properties from analysis ON THE NEW TYPED DOCUMENT
			CaseDocument.DocumentType documentTypeEnum = CaseDocument.DocumentType.valueOf(detectedType);
			CaseDocument.DocumentCategory documentCategoryEnum = CaseDocument.DocumentCategory
				.valueOf(detectedCategory);

			document.setDocumentType(documentTypeEnum);
			// The documentCategory is intrinsically part of the typedDocument (e.g.
			// QuestionnaireDocument's category is QUESTIONNAIRE)
			// and should be set correctly by the specific constructors or after
			// BeanUtils.copyProperties if the parent class has this field.
			// If CaseDocument has a discriminator column based on category, this might be
			// handled by JPA.
			// Let's ensure it's explicitly set on typedDocument if not already handled.
			document.setDocumentCategory(documentCategoryEnum);
			document.setMetadataJson(convertMapToJson(extractedData));
			document
				.setStatus(isComplete ? CaseDocument.DocumentStatus.COMPLETE : CaseDocument.DocumentStatus.INCOMPLETE);

			// Update description to include AI analysis results ON THE NEW TYPED DOCUMENT
			document.setDescription("Analyzed document: " + documentTypeEnum.getDisplayName() + " - "
					+ documentCategoryEnum.getDisplayName());

			// caseDocumentRepository.save(typedDocument); // This should not be needed if
			// cascade is set correctly

			legalCaseRepository.save(legalCase); // This will cascade save/update to
													// typedDocument

			// Trigger document completion events if needed
			if (isComplete) {
				// Use typedDocument's ID and title
				legalCase.markDocumentComplete(typedDocument.getId(), typedDocument.getTitle());
				eventPublisherFactory.publishEvents(legalCase);
			}

			log.info("Document analysis completed for ID: {}, new document type: {}, category: {}",
					typedDocument.getId(), documentTypeEnum, documentCategoryEnum); // Use
																					// typedDocument.getId()
		}
		catch (Exception e) {
			log.error("Error updating document after analysis: {}", documentId, e);
			updateDocumentWithError(documentId, "Error processing analysis results: " + e.getMessage());
		}
	}

	/**
	 * Update document with error information
	 * @param documentId document ID
	 * @param errorMessage error message
	 */
	@Transactional
	public void updateDocumentWithError(String documentId, String errorMessage) {
		try {
			// Find the document with its legal case eagerly loaded
			CaseDocument document = caseDocumentRepository.findByIdWithLegalCase(documentId)
				.orElseThrow(() -> new EntityNotFoundException("Document not found: " + documentId));

			// Just update the original document status rather than creating a new one
			document.setStatus(CaseDocument.DocumentStatus.REJECTED);
			document.setDescription("Analysis failed: " + errorMessage);

			Map<String, Object> errorData = new HashMap<>();
			errorData.put("error", errorMessage);
			errorData.put("timestamp", new Date().toString());
			document.setMetadataJson(convertMapToJson(errorData));

			// Save the document directly
			caseDocumentRepository.save(document);

			log.warn("Document marked as rejected due to error: {}", documentId);
		}
		catch (Exception e) {
			log.error("Error updating document with error status: {}", documentId, e);
		}
	}

	/**
	 * Copy base properties from source document to target document
	 * @param source source document
	 * @param target target document
	 */
	private void copyBaseProperties(CaseDocument source, CaseDocument target) {
		target.setId(source.getId());
		target.setTitle(source.getTitle());
		target.setDescription(source.getDescription());
		target.setFilePath(source.getFilePath());
		target.setFileType(source.getFileType());
		target.setFileSize(source.getFileSize());
		target.setStorageId(source.getStorageId());
		target.setCreatedBy(source.getCreatedBy());
		target.setCreatedAt(source.getCreatedAt());
		target.setUpdatedAt(source.getUpdatedAt());
		target.setLegalCase(source.getLegalCase());
		target.setDownloadUrl(source.getDownloadUrl());
	}

	/**
	 * Convert a map to JSON string
	 * @param map the map to convert
	 * @return JSON string representation
	 */
	private String convertMapToJson(Map<String, Object> map) {
		try {
			return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(map);
		}
		catch (Exception e) {
			log.error("Error converting map to JSON", e);
			return "{}";
		}
	}

}