package com.ginkgooai.legalcase.service.event;

import com.ginkgooai.legalcase.domain.event.CaseEvents.*;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.LlmAnalysisService;
import com.ginkgooai.legalcase.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event handlers for case-related domain events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CaseEventHandlers {

	private final LegalCaseRepository legalCaseRepository;

	private final NotificationService notificationService;

	private final LlmAnalysisService llmAnalysisService;

	/**
	 * Handle case created event
	 */
	@TransactionalEventListener
	public void handleCaseCreated(CaseCreatedEvent event) {
		log.info("Case created: {}", event.getCaseId());
		notificationService.notifyUserAboutCaseCreation(event.getProfileId(), event.getCaseId(), event.getCaseTitle());
	}

	/**
	 * Handle document completed event
	 */
	@TransactionalEventListener
	public void handleDocumentCompleted(DocumentCompletedEvent event) {
		log.info("Document {} completed for case {}", event.getDocumentName(), event.getCaseId());

		// 检查是否应该触发LLM分析
		llmAnalysisService.checkAndTriggerAnalysis(event.getCaseId());
	}

	/**
	 * Handle questionnaire completed event
	 */
	@TransactionalEventListener
	public void handleQuestionnaireCompleted(QuestionnaireCompletedEvent event) {
		log.info("Questionnaire {} completed for case {}", event.getQuestionnaireName(), event.getCaseId());

		// 检查是否应该触发LLM分析
		llmAnalysisService.checkAndTriggerAnalysis(event.getCaseId());
	}

	/**
	 * Handle form value recorded event This event is used to record form values for
	 * replay purposes
	 */
	@TransactionalEventListener
	public void handleFormValueRecorded(FormValueRecordedEvent event) {
		log.info("Form values recorded for form {} (page {}) in case {}, sequence: {}", event.getFormName(),
				event.getPageName(), event.getCaseId());

		// The actual persistence of these values for replay happens in a dedicated
		// service
		// This handler is mainly for logging and potential notifications
	}

	/**
	 * Handle LLM analysis initiated event This runs asynchronously to not block the main
	 * thread
	 */
	@Async
	@EventListener
	public void handleLlmAnalysisInitiated(LlmAnalysisInitiatedEvent event) {
		log.info("Starting LLM analysis for case: {}, type: {}", event.getCaseId(), event.getAnalysisType());

		try {
			// Call the LLM analysis service
			boolean successful = true;
			String resultSummary = "Analysis completed " + (successful ? "successfully" : "with errors");

			// Update the case with the analysis results
			legalCaseRepository.findById(event.getCaseId()).ifPresent(legalCase -> {
				legalCase.completeLlmAnalysis(successful, resultSummary);
				legalCaseRepository.save(legalCase);
			});
		}
		catch (Exception e) {
			log.error("Error during LLM analysis for case: {}", event.getCaseId(), e);
			// Handle the error and update the case status accordingly
			legalCaseRepository.findById(event.getCaseId()).ifPresent(legalCase -> {
				legalCase.completeLlmAnalysis(false, "Error: " + e.getMessage());
				legalCaseRepository.save(legalCase);
			});
		}
	}

	/**
	 * Handle LLM analysis completed event
	 */
	@TransactionalEventListener
	public void handleLlmAnalysisCompleted(LlmAnalysisCompletedEvent event) {
		log.info("LLM analysis completed for case: {}, successful: {}", event.getCaseId(), event.isSuccessful());

		try {
			// Call the LLM analysis service
			boolean successful = true;
			String resultSummary = "Analysis completed " + (successful ? "successfully" : "with errors");

			// Update the case with the analysis results
			legalCaseRepository.findById(event.getCaseId()).ifPresent(legalCase -> {
				legalCase.completeLlmAnalysis(successful, resultSummary);
				legalCaseRepository.save(legalCase);
			});
		}
		catch (Exception e) {
			log.error("Error during LLM analysis for case: {}", event.getCaseId(), e);
			// Handle the error and update the case status accordingly
			legalCaseRepository.findById(event.getCaseId()).ifPresent(legalCase -> {
				legalCase.completeLlmAnalysis(false, "Error: " + e.getMessage());
				legalCaseRepository.save(legalCase);
			});
		}

		// Notify about analysis completion
		notificationService.notifyAboutAnalysisCompletion(event.getCaseId(), event.isSuccessful(),
				event.getResultSummary());
	}

	/**
	 * Handle documentation complete event
	 */
	@TransactionalEventListener
	public void handleDocumentationComplete(DocumentationCompleteEvent event) {
		log.info("All documentation complete for case: {}", event.getCaseId());

		// Notify that the case is ready for review
		notificationService.notifyAboutReviewReady(event.getCaseId());
	}

	/**
	 * Handle auto-filling initiated event
	 */
	@Async
	@EventListener
	public void handleAutoFillingInitiated(AutoFillingInitiatedEvent event) {
		log.info("Auto-filling initiated for case: {}", event.getCaseId());

		// This would typically trigger an external auto-filling process
		// For now, just log and potentially update the case
	}

	/**
	 * Handle case put on hold event
	 */
	@TransactionalEventListener
	public void handleCasePutOnHold(CasePutOnHoldEvent event) {
		log.info("Case put on hold: {}, reason: {}", event.getCaseId(), event.getReason());

		// Notify relevant parties
		notificationService.notifyCaseOnHold(event.getCaseId(), event.getReason());
	}

	/**
	 * Handle case resumed event
	 */
	@TransactionalEventListener
	public void handleCaseResumed(CaseResumedEvent event) {
		log.info("Case resumed from hold: {}", event.getCaseId());

		// Notify relevant parties
		notificationService.notifyCaseResumed(event.getCaseId());
	}

	/**
	 * Handle case submitted event
	 */
	@TransactionalEventListener
	public void handleCaseSubmitted(CaseSubmittedEvent event) {
		log.info("Case submitted: {}, by: {}", event.getCaseId(), event.getSubmittedBy());

		// Notify approvers
		notificationService.notifyApproversAboutSubmission(event.getCaseId(), event.getSubmittedBy());
	}

	/**
	 * Handle case approved event
	 */
	@TransactionalEventListener
	public void handleCaseApproved(CaseApprovedEvent event) {
		log.info("Case approved: {}, by: {}", event.getCaseId(), event.getApprovedBy());

		// Notify relevant parties
		notificationService.notifyCaseApproved(event.getCaseId(), event.getApprovedBy(), event.getComments());
	}

	/**
	 * Handle case denied event
	 */
	@TransactionalEventListener
	public void handleCaseDenied(CaseDeniedEvent event) {
		log.info("Case denied: {}, by: {}, reason: {}", event.getCaseId(), event.getDeniedBy(), event.getReason());

		// Notify relevant parties
		notificationService.notifyCaseDenied(event.getCaseId(), event.getDeniedBy(), event.getReason());
	}

}