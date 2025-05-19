package com.ginkgooai.legalcase.service.event;

import com.ginkgooai.legalcase.domain.CaseStatus;
import com.ginkgooai.legalcase.domain.LegalCase;
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
 * Event handlers for case domain events
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
	 * @param event case created event
	 */
	@TransactionalEventListener
	public void handleCaseCreated(CaseCreatedEvent event) {
		log.info("Handling case created event: {}", event.getCaseId());

		// Notify users about case creation
		notificationService.notifyUserAboutCaseCreation(event.getProfileId(), event.getCaseId(), event.getCaseTitle());
	}

	/**
	 * Handle document completed event
	 * @param event document completed event
	 */
	@TransactionalEventListener
	public void handleDocumentCompleted(DocumentCompletedEvent event) {
		log.info("Handling document completed event: {}", event.getCaseId());

		// Check if LLM analysis should be triggered
		llmAnalysisService.checkAndTriggerAnalysis(event.getCaseId());
	}

	/**
	 * Handle questionnaire completed event
	 * @param event questionnaire completed event
	 */
	@TransactionalEventListener
	public void handleQuestionnaireCompleted(QuestionnaireCompletedEvent event) {
		log.info("Handling questionnaire completed event: {}", event.getCaseId());

		// Check if LLM analysis should be triggered
		llmAnalysisService.checkAndTriggerAnalysis(event.getCaseId());
	}

	/**
	 * Handle form value recorded event
	 * @param event form value recorded event
	 */
	@TransactionalEventListener
	public void handleFormValueRecorded(FormValueRecordedEvent event) {
		log.debug("Handling form value recorded event: {} - {}.{}", event.getCaseId(), event.getFormId(),
				event.getInputId());

		// Form values are recorded but don't typically trigger notifications or status
		// changes
		// Could be used for analytics, form completion tracking, etc.
	}

	/**
	 * Handle LLM analysis initiated event
	 * @param event LLM analysis initiated event
	 */
	@Async
	@EventListener
	public void handleLlmAnalysisInitiated(LlmAnalysisInitiatedEvent event) {
		log.info("Handling LLM analysis initiated event: {}", event.getCaseId());

		try {
			// This is where you would integrate with any LLM/AI services
			// For demonstration, just simulate analysis time
			Thread.sleep(1000); // Simulate AI processing

			// Get the case
			LegalCase legalCase = legalCaseRepository.findById(event.getCaseId()).orElse(null);
			if (legalCase == null) {
				log.error("Case not found for LLM analysis: {}", event.getCaseId());
				return;
			}

			// Complete the analysis with success
			legalCase.completeLlmAnalysis(true, "Analysis completed successfully");
			legalCaseRepository.save(legalCase);
			eventPublisherFactory.publishEvents(legalCase);

			log.info("LLM analysis completed for case: {}", event.getCaseId());
		}
		catch (Exception e) {
			log.error("Error during LLM analysis for case: {}", event.getCaseId(), e);
		}
	}

	/**
	 * Handle LLM analysis completed event
	 * @param event LLM analysis completed event
	 */
	@TransactionalEventListener
	public void handleLlmAnalysisCompleted(LlmAnalysisCompletedEvent event) {
		log.info("Handling LLM analysis completed event: {}, successful: {}", event.getCaseId(), event.isSuccessful());

		// Notify about analysis completion
		notificationService.notifyAboutAnalysisCompletion(event.getCaseId(), event.isSuccessful(),
				event.getResultSummary());

		// If the analysis was successful and we have a case that is documentation
		// complete
		LegalCase legalCase = legalCaseRepository.findById(event.getCaseId()).orElse(null);
		if (legalCase != null && legalCase.getStatus() == CaseStatus.DOCUMENTATION_COMPLETE) {
			// Notify that the case is ready for review
			notificationService.notifyAboutReviewReady(event.getCaseId());
		}
	}

	/**
	 * Handle documentation complete event
	 * @param event documentation complete event
	 */
	@TransactionalEventListener
	public void handleDocumentationComplete(DocumentationCompleteEvent event) {
		log.info("Handling documentation complete event: {}", event.getCaseId());

		// Case is now ready for the next phase (auto-filling)
		LegalCase legalCase = legalCaseRepository.findById(event.getCaseId()).orElse(null);
		if (legalCase != null) {
			// Could automatically start auto-filling here, but we'll leave it as a manual
			// step
			notificationService.notifyAboutReviewReady(event.getCaseId());
		}
	}

	/**
	 * Handle auto-filling initiated event
	 * @param event auto-filling initiated event
	 */
	@Async
	@EventListener
	public void handleAutoFillingInitiated(AutoFillingInitiatedEvent event) {
		log.info("Handling auto-filling initiated event: {}", event.getCaseId());

		// This would be where you integrate with document auto-filling systems
		// For demonstration, just simulate processing time
		try {
			// Simulate processing
			Thread.sleep(2000);

			// Complete auto-filling
			LegalCase legalCase = legalCaseRepository.findById(event.getCaseId()).orElse(null);
			if (legalCase != null) {
				legalCase.completeAutoFilling();
				legalCaseRepository.save(legalCase);
				eventPublisherFactory.publishEvents(legalCase);
			}
		}
		catch (Exception e) {
			log.error("Error during auto-filling for case: {}", event.getCaseId(), e);
		}
	}

	/**
	 * Handle case put on hold event
	 * @param event case put on hold event
	 */
	@TransactionalEventListener
	public void handleCasePutOnHold(CasePutOnHoldEvent event) {
		log.info("Handling case put on hold event: {}, reason: {}", event.getCaseId(), event.getReason());

		// Notify relevant parties that case is on hold
		notificationService.notifyCaseOnHold(event.getCaseId(), event.getReason());
	}

	/**
	 * Handle case resumed event
	 * @param event case resumed event
	 */
	@TransactionalEventListener
	public void handleCaseResumed(CaseResumedEvent event) {
		log.info("Handling case resumed event: {}", event.getCaseId());

		// Notify relevant parties that case is resumed
		notificationService.notifyCaseResumed(event.getCaseId());
	}

	/**
	 * Handle case submitted event
	 * @param event case submitted event
	 */
	@TransactionalEventListener
	public void handleCaseSubmitted(CaseSubmittedEvent event) {
		log.info("Handling case submitted event: {}, submitted by: {}", event.getCaseId(), event.getSubmittedBy());

		// Notify approvers that a case is ready for review
		notificationService.notifyApproversAboutSubmission(event.getCaseId(), event.getSubmittedBy());
	}

	/**
	 * Handle case approved event
	 * @param event case approved event
	 */
	@TransactionalEventListener
	public void handleCaseApproved(CaseApprovedEvent event) {
		log.info("Handling case approved event: {}, approved by: {}", event.getCaseId(), event.getApprovedBy());

		// Notify relevant parties that case is approved
		notificationService.notifyCaseApproved(event.getCaseId(), event.getApprovedBy(), event.getComments());
	}

	/**
	 * Handle case denied event
	 * @param event case denied event
	 */
	@TransactionalEventListener
	public void handleCaseDenied(CaseDeniedEvent event) {
		log.info("Handling case denied event: {}, denied by: {}, reason: {}", event.getCaseId(), event.getDeniedBy(),
				event.getReason());

		// Notify relevant parties that case is denied
		notificationService.notifyCaseDenied(event.getCaseId(), event.getDeniedBy(), event.getReason());
	}

	private final DomainEventPublisherFactory eventPublisherFactory;
}