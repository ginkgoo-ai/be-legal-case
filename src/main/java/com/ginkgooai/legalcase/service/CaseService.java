package com.ginkgooai.legalcase.service;

import com.ginkgooai.core.common.exception.ResourceNotFoundException;
import com.ginkgooai.legalcase.domain.*;
import com.ginkgooai.legalcase.dto.CaseDocumentResponse;
import com.ginkgooai.legalcase.dto.LegalCaseResponse;
import com.ginkgooai.legalcase.dto.UpdateLegalCaseRequest;
import com.ginkgooai.legalcase.repository.CaseDocumentRepository;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.event.DomainEventPublisherFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for optimized case creation process
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaseService {

	private final LegalCaseRepository legalCaseRepository;

	private final CaseDocumentRepository caseDocumentRepository;

	private final DomainEventPublisherFactory eventPublisherFactory;

	/**
	 * Find a document by its ID
	 * @param documentId document ID
	 * @return optional containing the document if found
	 */
	@Transactional(readOnly = true)
	public Optional<CaseDocument> findDocumentById(String documentId) {
		return caseDocumentRepository.findById(documentId);
	}

	/**
	 * Step 1: Create an empty case
	 * @param title case title
	 * @param description case description
	 * @param profileId owner's profile ID
	 * @param clientId client ID
	 * @return the created case
	 */
	@Transactional
	public LegalCase createEmptyCase(String title, String description, String profileId, String clientId) {
		log.info("Creating empty case with title: {}", title);

		LegalCase legalCase = new LegalCase(clientId, profileId, title, description); // Triggers
																						// domain
																						// events
		LegalCase savedCase = legalCaseRepository.save(legalCase);
		eventPublisherFactory.publishEvents(savedCase);

		log.info("Empty case created with ID: {}", savedCase.getId());
		return savedCase;
	}

	/**
	 * Step 2: Select questionnaire template and generate link
	 * @param caseId case ID
	 * @param templateIds list of template IDs to use
	 * @return map of template IDs to questionnaire links
	 */
	@Transactional
	public Map<String, String> selectQuestionnaireTemplates(String caseId, List<String> templateIds) {
		log.info("Selecting questionnaire templates for case: {}", caseId);

		LegalCase legalCase = legalCaseRepository.findById(caseId)
			.orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));

		// Create questionnaire documents from templates
		Map<String, String> templateLinks = templateIds.stream()
			.collect(java.util.stream.Collectors.toMap(templateId -> templateId, templateId -> {
				QuestionnaireDocument questionnaire = createQuestionnaireFromTemplate(legalCase, templateId);
				legalCase.addQuestionnaireDocument(questionnaire);
				return generateQuestionnaireLink(questionnaire.getId());
			}));

		legalCaseRepository.save(legalCase);
		eventPublisherFactory.publishEvents(legalCase);

		log.info("Generated {} questionnaire links for case: {}", templateLinks.size(), caseId);
		return templateLinks;
	}

	private String generateQuestionnaireLink(String id) {
		return "";
	}

	private QuestionnaireDocument createQuestionnaireFromTemplate(LegalCase legalCase, String templateId) {
		// In a real implementation, this would fetch the template details from a
		// repository
		QuestionnaireDocument questionnaire = new QuestionnaireDocument();
		questionnaire.setTitle("Questionnaire from template " + templateId);
		questionnaire.setDescription("Generated from template");
		questionnaire.setQuestionnaireType(templateId);
		questionnaire.setStatus(CaseDocument.DocumentStatus.PENDING);
		questionnaire.setCompletionPercentage(0);
		return questionnaire;
	}

	@Transactional(readOnly = true)
	public LegalCaseResponse getLegalCase(String caseId) {
		LegalCase legalCase = legalCaseRepository.findByIdWithDocuments(caseId)
			.orElseThrow(() -> new ResourceNotFoundException("Legal case", "caseId", caseId));
		return convertToResponse(legalCase);
	}

	@Transactional
	public LegalCaseResponse updateLegalCase(String caseId, UpdateLegalCaseRequest request) {
		LegalCase legalCase = findLegalCaseById(caseId);

		if (request.getTitle() != null) {
			legalCase.setTitle(request.getTitle());
		}

		if (request.getDescription() != null) {
			legalCase.setDescription(request.getDescription());
		}

		LegalCase updatedCase = legalCaseRepository.save(legalCase);
		return convertToResponse(updatedCase);
	}

	@Transactional
	public void deleteLegalCase(String caseId) {
		LegalCase legalCase = findLegalCaseById(caseId);
		legalCaseRepository.delete(legalCase);
	}

	@Transactional(readOnly = true)
	public Page<LegalCaseResponse> getLegalCasesByProfileId(String profileId, Pageable pageable) {
		Page<LegalCase> cases = legalCaseRepository.findByProfileId(profileId, pageable);
		return cases.map(this::convertToResponse);
	}

	@Transactional(readOnly = true)
	public Page<LegalCaseResponse> getLegalCasesByClientId(String clientId, Pageable pageable) {
		Page<LegalCase> cases = legalCaseRepository.findByClientId(clientId, pageable);
		return cases.map(this::convertToResponse);
	}

	@Transactional(readOnly = true)
	public Page<LegalCaseResponse> searchLegalCases(String searchTerm, Pageable pageable) {
		Page<LegalCase> cases = legalCaseRepository.searchCases(searchTerm, pageable);
		return cases.map(this::convertToResponse);
	}

	public LegalCaseResponse convertToResponse(LegalCase legalCase) {
		LegalCaseResponse response = LegalCaseResponse.builder()
			.id(legalCase.getId())
			.title(legalCase.getTitle())
			.description(legalCase.getDescription())
			.profileId(legalCase.getProfileId())
			.status(legalCase.getStatus())
			.createdAt(legalCase.getCreatedAt())
			.updatedAt(legalCase.getUpdatedAt())
			.documentsCount(legalCase.getDocuments().size())
			.documents(legalCase.getDocuments().stream().map(this::convertToDocumentResponse).toList())
			.build();

		return response;
	}

	private CaseDocumentResponse convertToDocumentResponse(CaseDocument document) {
		return CaseDocumentResponse.builder()
			.id(document.getId())
			.title(document.getTitle())
			.description(document.getDescription())
			.filePath(document.getFilePath())
			.fileType(document.getFileType())
			.fileSize(document.getFileSize())
			.storageId(document.getStorageId())
			.caseId(document.getLegalCase().getId())
			.documentType(document.getDocumentType())
			.downloadUrl(document.getDownloadUrl())
			.createdAt(document.getCreatedAt())
			.updatedAt(document.getUpdatedAt())
			.metadataJson(document.getMetadataJson())
			.build();
	}

	private LegalCase findLegalCaseById(String caseId) {
		return legalCaseRepository.findById(caseId)
			.orElseThrow(() -> new ResourceNotFoundException("Legal case", "caseId", caseId));
	}

}