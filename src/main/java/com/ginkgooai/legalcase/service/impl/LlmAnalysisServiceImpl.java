package com.ginkgooai.legalcase.service.impl;

import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.repository.EventLogRepository;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.LlmAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Implementation of LLM analysis service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlmAnalysisServiceImpl implements LlmAnalysisService {

	private final LegalCaseRepository legalCaseRepository;

	private final EventLogRepository eventLogRepository;

	private static final String LLM_ANALYSIS_INITIATED_EVENT = "LlmAnalysisInitiated";

	private static final int MIN_ANALYSIS_INTERVAL_HOURS = 1;

	/**
	 * Check and trigger LLM analysis if necessary
	 * @param caseId case ID
	 * @return whether analysis was triggered
	 */
	@Override
	@Transactional
	public boolean checkAndTriggerAnalysis(String caseId) {
		log.info("Checking if LLM analysis should be triggered for case: {}", caseId);

		LegalCase legalCase = legalCaseRepository.findByIdWithDocuments(caseId).orElse(null);
		if (legalCase == null) {
			log.warn("Case not found for LLM analysis check: {}", caseId);
			return false;
		}

		if (shouldPerformAnalysis(legalCase)) {
			log.info("Triggering LLM analysis for case: {}", caseId);
			legalCase.initiateLlmAnalysis("document_analysis");
			legalCaseRepository.save(legalCase);
			return true;
		}

		log.info("LLM analysis not needed for case: {}", caseId);
		return false;
	}

	/**
	 * Check if a case should undergo LLM analysis
	 * @param legalCase legal case
	 * @return whether analysis should be performed
	 */
	@Override
	public boolean shouldPerformAnalysis(LegalCase legalCase) {
		// Check if there are completed documents
		if (!legalCase.hasCompletedDocumentsForAnalysis()) {
			log.info("No completed documents for analysis in case: {}", legalCase.getId());
			return false;
		}

		// Check last analysis time
		LocalDateTime lastAnalysisTime = getLastAnalysisTime(legalCase.getId());

		// If no previous analysis or enough time has passed since last analysis
		if (lastAnalysisTime == null) {
			log.info("No previous analysis for case: {}", legalCase.getId());
			return true;
		}

		LocalDateTime now = LocalDateTime.now();
		long hoursSinceLastAnalysis = ChronoUnit.HOURS.between(lastAnalysisTime, now);
		log.info("Hours since last analysis for case {}: {}", legalCase.getId(), hoursSinceLastAnalysis);

		return hoursSinceLastAnalysis >= MIN_ANALYSIS_INTERVAL_HOURS;
	}

	/**
	 * Get the time of the last LLM analysis
	 * @param caseId case ID
	 * @return time of the last analysis
	 */
	@Override
	public LocalDateTime getLastAnalysisTime(String caseId) {
		// Query the last LLM analysis event time from event logs
		Optional<LocalDateTime> lastEventTime = eventLogRepository.findLastEventTimeByTypeAndCaseId(caseId,
				LLM_ANALYSIS_INITIATED_EVENT);

		return lastEventTime.orElse(null);
	}

}