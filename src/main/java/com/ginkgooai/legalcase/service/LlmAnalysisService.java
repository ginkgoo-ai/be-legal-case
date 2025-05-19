package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.domain.LegalCase;

/**
 * Service for managing LLM analysis of legal cases
 */
public interface LlmAnalysisService {

	/**
	 * Check and trigger LLM analysis if necessary
	 * @param caseId case ID
	 * @return whether analysis was triggered
	 */
	boolean checkAndTriggerAnalysis(String caseId);

	/**
	 * Check if a case should undergo LLM analysis
	 * @param legalCase legal case
	 * @return whether analysis should be performed
	 */
	boolean shouldPerformAnalysis(LegalCase legalCase);

	/**
	 * Get the time of the last LLM analysis
	 * @param caseId case ID
	 * @return time of the last analysis
	 */
	java.time.LocalDateTime getLastAnalysisTime(String caseId);

}