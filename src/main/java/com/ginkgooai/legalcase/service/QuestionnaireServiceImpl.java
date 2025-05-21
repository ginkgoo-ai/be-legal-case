package com.ginkgooai.legalcase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.core.common.utils.ContextUtils;
import com.ginkgooai.legalcase.client.storage.StorageClient;
import com.ginkgooai.legalcase.client.storage.dto.CloudFileResponse;
import com.ginkgooai.legalcase.domain.CaseDocument;
import com.ginkgooai.legalcase.domain.DocumentType;
import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.domain.QuestionnaireDocument;
import com.ginkgooai.legalcase.dto.QuestionnaireSubmissionRequest;
import com.ginkgooai.legalcase.dto.QuestionnaireSubmissionResponse;
import com.ginkgooai.legalcase.repository.CaseDocumentRepository;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.event.DomainEventPublisherFactory;
import com.ginkgooai.legalcase.util.MultipartFileUtils;
import com.ginkgooai.legalcase.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionnaireServiceImpl implements QuestionnaireService {

	private final LegalCaseRepository legalCaseRepository;

	private final CaseDocumentRepository caseDocumentRepository;

	private final StorageClient storageClient;

	private final DomainEventPublisherFactory eventPublisherFactory;

	private final ObjectMapper objectMapper;

	@Override
	@Transactional
	public QuestionnaireSubmissionResponse processQuestionnaireSubmission(QuestionnaireSubmissionRequest request) {
		log.info("Processing questionnaire submission: questionnaireId={}, caseId={}", request.getQuestionnaireId(),
				request.getCaseId());

		// Find the case
		LegalCase legalCase = legalCaseRepository.findById(request.getCaseId())
			.orElseThrow(() -> new ResourceNotFoundException("Legal case", "caseId", request.getCaseId()));

		// Generate questionnaire PDF
		String title = "Questionnaire Response: " + request.getQuestionnaireId();
		byte[] pdfData = PdfGenerator.generateQuestionnairePdf(request.getQuestionnaireId(), request.getResponses(),
				title);

		// Upload PDF to storage service
		String filename = request.getQuestionnaireName() + "_" + System.currentTimeMillis() + ".pdf";
		MultipartFile multipartFile = MultipartFileUtils.createMultipartFile(pdfData, filename, "application/pdf");
		ResponseEntity<CloudFileResponse> uploadResponse = storageClient.uploadFile(multipartFile);
		CloudFileResponse fileResponse = uploadResponse.getBody();

		if (fileResponse == null || fileResponse.getId() == null) {
			log.error("Failed to upload questionnaire PDF to storage service");
			return createErrorResponse(request, "Failed to upload questionnaire PDF");
		}

		// Create questionnaire document
		QuestionnaireDocument questionnaireDoc = new QuestionnaireDocument();
		questionnaireDoc.setTitle(title);
		questionnaireDoc.setDescription("Questionnaire submission response");
		questionnaireDoc.setQuestionnaireType(request.getQuestionnaireType());
		questionnaireDoc.setStorageId(fileResponse.getId());
		questionnaireDoc.setFilePath(fileResponse.getStoragePath());
		questionnaireDoc.setFileType("application/pdf");
		questionnaireDoc.setFileSize(fileResponse.getFileSize());
		questionnaireDoc.setStatus(CaseDocument.DocumentStatus.COMPLETE);
		questionnaireDoc.setDocumentType(DocumentType.QUESTIONNAIRE);
		questionnaireDoc.setDocumentCategory(CaseDocument.DocumentCategory.QUESTIONNAIRE);
		questionnaireDoc.setCompletionPercentage(100);

		// Set questionnaire responses as metadataJson
		try {
			String responsesJson = objectMapper.writeValueAsString(request.getResponses());
			questionnaireDoc.setMetadataJson(responsesJson);
		}
		catch (JsonProcessingException e) {
			log.error("Failed to serialize questionnaire responses", e);
			return createErrorResponse(request, "Failed to process questionnaire responses");
		}

		// Add to case
		legalCase.addQuestionnaireDocument(questionnaireDoc);

		// Save and publish events
		legalCaseRepository.save(legalCase);
		eventPublisherFactory.publishEvents(legalCase);

		// Create success response
		return QuestionnaireSubmissionResponse.builder()
			.id(questionnaireDoc.getId())
			.questionnaireId(request.getQuestionnaireId())
			.userId(ContextUtils.getUserId())
			.caseId(request.getCaseId())
			.status("COMPLETED")
			.submittedAt(LocalDateTime.now())
			.message("Questionnaire submitted successfully")
			.build();
	}

	private QuestionnaireSubmissionResponse createErrorResponse(QuestionnaireSubmissionRequest request,
			String errorMessage) {
		return QuestionnaireSubmissionResponse.builder()
			.questionnaireId(request.getQuestionnaireId())
			.userId(ContextUtils.getUserId())
			.caseId(request.getCaseId())
			.status("FAILED")
			.submittedAt(LocalDateTime.now())
			.message(errorMessage)
			.build();
	}
}
