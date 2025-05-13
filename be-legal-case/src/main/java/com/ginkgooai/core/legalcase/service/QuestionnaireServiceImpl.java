package com.ginkgooai.core.legalcase.service;

import com.ginkgooai.core.legalcase.client.gatekeeper.GatekeeperClient;
import com.ginkgooai.core.legalcase.client.gatekeeper.dto.QuestionnaireResponseDTO;
import com.ginkgooai.core.legalcase.domain.LegalCase;
import com.ginkgooai.core.legalcase.dto.QuestionnaireSubmissionRequest;
import com.ginkgooai.core.legalcase.dto.QuestionnaireSubmissionResponse;
import com.ginkgooai.core.legalcase.exception.ResourceNotFoundException;
import com.ginkgooai.core.legalcase.repository.LegalCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionnaireServiceImpl implements QuestionnaireService {

	private final GatekeeperClient gatekeeperClient;

	private final LegalCaseRepository legalCaseRepository;

	private final LegalCaseService legalCaseService;

	@Override
	@Transactional
	public QuestionnaireSubmissionResponse processQuestionnaireSubmission(QuestionnaireSubmissionRequest request) {
		log.info("Processing questionnaire submission: userId={}, questionnaireId={}", request.getUserId(),
				request.getQuestionnaireId());

		try {
			// Save questionnaire response to Gatekeeper service
			QuestionnaireResponseDTO responseDTO = saveQuestionnaireResponse(request);

			// If there is an associated case ID, update the case information
			if (request.getCaseId() != null) {
				updateCaseWithQuestionnaireData(request.getCaseId(), responseDTO);
			}

			return QuestionnaireSubmissionResponse.builder()
				.id(responseDTO.getId())
				.questionnaireId(request.getQuestionnaireId())
				.userId(request.getUserId())
				.caseId(request.getCaseId())
				.status("COMPLETED")
				.submittedAt(LocalDateTime.now())
				.message("Questionnaire submitted successfully")
				.build();

		}
		catch (Exception e) {
			log.error("Failed to process questionnaire submission", e);
			return QuestionnaireSubmissionResponse.builder()
				.questionnaireId(request.getQuestionnaireId())
				.userId(request.getUserId())
				.caseId(request.getCaseId())
				.status("FAILED")
				.submittedAt(LocalDateTime.now())
				.message("Failed to process questionnaire submission: " + e.getMessage())
				.build();
		}
	}

	// Save questionnaire response to Gatekeeper service
	private QuestionnaireResponseDTO saveQuestionnaireResponse(QuestionnaireSubmissionRequest request) {
		// Build questionnaire response DTO
		QuestionnaireResponseDTO responseDTO = QuestionnaireResponseDTO.builder()
			.id(UUID.randomUUID().toString())
			.questionnaireId(request.getQuestionnaireId())
			.userId(request.getUserId())
			.questionnaireType(request.getQuestionnaireType())
			.status("COMPLETED")
			.responses(request.getResponses())
			.submittedAt(LocalDateTime.now())
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		// Call Gatekeeper service to save questionnaire response
		try {
			return gatekeeperClient.saveQuestionnaireResponse(responseDTO);
		}
		catch (Exception e) {
			log.error("Failed to call Gatekeeper service to save questionnaire response", e);
			// If the call fails, return the locally built DTO
			return responseDTO;
		}
	}

	// Update case information with questionnaire data
	private void updateCaseWithQuestionnaireData(String caseId, QuestionnaireResponseDTO responseDTO) {
		// Get the case
		LegalCase legalCase = legalCaseRepository.findById(caseId)
			.orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));

		// Get questionnaire response data
		Map<String, Object> responses = responseDTO.getResponses();

		// Process differently based on questionnaire type
		// This is just an example, the actual business logic may be more complex
		if ("LEGAL_INTAKE".equals(responseDTO.getQuestionnaireType())) {
			// Add some key information from the questionnaire to the case description
			StringBuilder enhancedDescription = new StringBuilder();

			if (legalCase.getDescription() != null && !legalCase.getDescription().isBlank()) {
				enhancedDescription.append(legalCase.getDescription()).append("\n\n");
			}

			enhancedDescription.append("Additional information from client questionnaire:\n");

			// Example: Extract information from questionnaire
			if (responses.containsKey("legalIssueCategory")) {
				enhancedDescription.append("Legal issue category: ")
					.append(responses.get("legalIssueCategory"))
					.append("\n");
			}

			if (responses.containsKey("incidentDate")) {
				enhancedDescription.append("Incident date: ").append(responses.get("incidentDate")).append("\n");
			}

			if (responses.containsKey("caseBackground")) {
				enhancedDescription.append("Case background: ").append(responses.get("caseBackground")).append("\n");
			}

			legalCase.setDescription(enhancedDescription.toString());
			legalCaseRepository.save(legalCase);
		}
	}

}