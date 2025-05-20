package com.ginkgooai.legalcase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.legalcase.client.storage.StorageClient;
import com.ginkgooai.legalcase.client.storage.dto.CloudFileResponse;
import com.ginkgooai.legalcase.domain.CaseDocument;
import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.domain.SupportingDocument;
import com.ginkgooai.legalcase.domain.event.CaseEvents;
import com.ginkgooai.legalcase.repository.CaseDocumentRepository;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.ai.DocumentAnalysisService;
import com.ginkgooai.legalcase.service.event.DomainEventPublisherFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Service for managing case documents
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
		return caseDocumentRepository.findByIdWithLegalCase(documentId);
	}

	/**
	 * Get all documents for a case
	 * @param caseId case ID
	 * @return list of documents
	 */
	@Transactional(readOnly = true)
	public List<CaseDocument> getDocumentsByCaseId(String caseId) {
		return caseDocumentRepository.findAllByCaseId(caseId);
	}

	/**
	 * Upload documents to a case
	 * @param caseId case ID
	 * @param storageIds list of storage IDs
	 * @return list of created document IDs
	 */
	@Transactional
	public List<String> uploadDocuments(String caseId, List<String> storageIds) {
		log.info("Uploading {} documents for case: {}", storageIds.size(), caseId);

		// Fetch the legal case
		LegalCase legalCase = legalCaseRepository.findById(caseId)
			.orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));

		ResponseEntity<List<CloudFileResponse>> fileDetails = storageClient.getFileDetails(storageIds);
		Map<String, CloudFileResponse> fileDetailsMap = fileDetails.getBody()
			.stream()
			.collect(Collectors.toMap(CloudFileResponse::getId, file -> file));

		// Create a document for each storage ID
		List<String> documentIds = new ArrayList<>();
		for (String storageId : storageIds) {
			CloudFileResponse fileInfo = fileDetailsMap.get(storageId);
			if (fileInfo == null) {
				log.warn("No file details found for storage ID: {}", storageId);
				continue;
			}

			// First check for and remove any documents with the same storage ID
			List<CaseDocument> existingDocuments = caseDocumentRepository.findByCaseIdAndStorageId(caseId, storageId);
			for (CaseDocument existingDoc : existingDocuments) {
				// Remove from the case's document collection
				legalCase.getDocuments().remove(existingDoc);
			}

			// Create a supporting document by default
			SupportingDocument document = new SupportingDocument();
			document.setStorageId(storageId);
			document.setTitle(fileInfo.getOriginalName());
			document.setDescription("Uploaded document");
			document.setFilePath(fileInfo.getStoragePath());
			document.setFileType(fileInfo.getFileType());
			document.setFileSize(fileInfo.getFileSize());
			document.setStatus(CaseDocument.DocumentStatus.PENDING);
			document.setDocumentType(CaseDocument.DocumentType.OTHER);

			// Add to case
			legalCase.addSupportingDocument(document);
			documentIds.add(document.getId());
		}

		// Save the case
		LegalCase savedLegalCase = legalCaseRepository.save(legalCase);
		eventPublisherFactory.publishEvents(legalCase);

		// 使用TransactionSynchronizationManager在事务提交后执行异步操作
		final List<CaseDocument> pendingDocuments = savedLegalCase.getDocuments()
			.stream()
			.filter(doc -> doc.getStatus() == CaseDocument.DocumentStatus.PENDING)
			.collect(Collectors.toList());
		documentIds = pendingDocuments.stream().map(CaseDocument::getId).collect(Collectors.toList());

		log.info("Created {} documents for case: {}", pendingDocuments.size(), caseId);

		// After transaction is complete, queue documents for analysis
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			List<String> finalDocumentIds = documentIds;
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
				@Override
				public void afterCommit() {
					for (String documentId : finalDocumentIds) {
						CaseDocument document = caseDocumentRepository.findById(documentId).orElse(null);
						if (document == null) {
							continue;
						}

						// Get presigned URL for analysis
						try {
							CloudFileResponse fileInfo = fileDetailsMap.get(document.getStorageId());

							ResponseEntity<URL> urlResponse = storageClient
								.generatePresignedUrl(fileInfo.getStorageName());
							URL presignedUrl = urlResponse.getBody();
							log.debug("Uploading presigned url: {}", presignedUrl);
							CompletableFuture.runAsync(() -> {
								queueDocumentForAnalysis(caseId, documentId, presignedUrl.toString());
							});
						}
						catch (Exception e) {
							log.error("Error generating presigned URL for document: {}", documentId, e);
						}
					}
				}
			});
		}

		log.info("Uploaded {} documents for case: {}", documentIds.size(), caseId);
		return documentIds;
	}

	/**
	 * Queue a document for analysis
	 * @param caseId case ID
	 * @param documentId document ID
	 * @param publicUrl public URL of the document
	 */
	@Async
	public void queueDocumentForAnalysis(String caseId, String documentId, String publicUrl) {
		log.info("Queueing document for analysis: {} with URL: {}", documentId, publicUrl);

		// Create callback to handle analysis result
		BiConsumer<Pair, Map<String, Object>> callback = this::updateDocumentAfterAnalysis;

		// Start the analysis
		documentAnalysisService.analyzeDocument(caseId, documentId, publicUrl, callback);
	}

	/**
	 * Update document after analysis
	 * @param pair pair of caseId and documentId
	 * @param analysisResult analysis result
	 */
	@Transactional
	public void updateDocumentAfterAnalysis(Pair pair, Map<String, Object> analysisResult) {
		String caseId = (String) pair.getLeft();
		String documentId = (String) pair.getRight();

		log.info("Updating document after analysis: {}", documentId);

		// Get the document
		LegalCase legalCase = legalCaseRepository.findByIdWithDocuments(caseId)
			.orElseThrow(() -> new ResourceNotFoundException("Legal case", "caseId", caseId));

		CaseDocument document = legalCase.getDocuments()
			.stream()
			.filter(doc -> doc.getId().equals(documentId))
			.findFirst()
			.orElseThrow(() -> new ResourceNotFoundException("Case document", "caseId-documentId",
					String.join("-", caseId, documentId)));

		// Check for errors
		if (analysisResult.containsKey("error")) {
			String errorMessage = (String) analysisResult.get("error");
			log.error("Error analyzing document {}: {}", documentId, errorMessage);
			document.setStatus(CaseDocument.DocumentStatus.REJECTED);
			caseDocumentRepository.save(document);
			return;
		}

		// Get document type and category
		String documentType = (String) analysisResult.getOrDefault("documentType", "OTHER");
		String documentCategory = (String) analysisResult.getOrDefault("documentCategory", "SUPPORTING_DOCUMENT");
		boolean isComplete = (boolean) analysisResult.getOrDefault("isComplete", false);
		Map<String, Object> extractedData = (Map<String, Object>) analysisResult.getOrDefault("extractedData",
				new HashMap<>());

		document.setDocumentCategory(CaseDocument.DocumentCategory.valueOf(documentCategory));
		document.setMetadataJson(convertMapToJson(extractedData));
		document.setDocumentType(CaseDocument.DocumentType.valueOf(documentType));

		legalCaseRepository.save(legalCase);

		// If document is complete, fire event
		if (isComplete) {
			legalCase.registerEvent(new CaseEvents.DocumentCompletedEvent(caseId, documentId, document.getTitle()));
			eventPublisherFactory.publishEvents(legalCase);
		}
	}

	/**
	 * Update document with error
	 * @param documentId document ID
	 * @param errorMessage error message
	 */
	@Transactional
	public void updateDocumentWithError(String documentId, String errorMessage) {
		log.error("Updating document with error: {}", documentId);

		// Get the document
		CaseDocument document = caseDocumentRepository.findByIdWithLegalCase(documentId)
			.orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

		// Update status and save
		document.setStatus(CaseDocument.DocumentStatus.REJECTED);
		document.setMetadataJson("{\"error\": \"" + errorMessage + "\"}");
		caseDocumentRepository.save(document);

		log.info("Document {} marked as rejected due to error", documentId);
	}

	/**
	 * Convert a map to JSON
	 * @param map map to convert
	 * @return JSON string
	 */
	private String convertMapToJson(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return "{}";
		}

		try {
			return new ObjectMapper().writeValueAsString(map);
		}
		catch (Exception e) {
			log.error("Error converting map to JSON", e);
			return "{}";
		}
	}
}