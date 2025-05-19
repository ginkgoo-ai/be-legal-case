package com.ginkgooai.legalcase.service.impl;

import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.domain.event.EventLog;
import com.ginkgooai.legalcase.repository.EventLogRepository;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.LlmAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * LLM分析服务实现 Implementation of LLM analysis service
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
	 * 检查并在必要时触发LLM分析 Check and trigger LLM analysis if necessary
	 */
	@Override
	@Transactional
	public boolean checkAndTriggerAnalysis(String caseId) {
		log.debug("Checking if case {} should undergo LLM analysis", caseId);

		LegalCase legalCase = legalCaseRepository.findById(caseId)
			.orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));

		if (shouldPerformAnalysis(legalCase)) {
			log.info("Triggering LLM analysis for case {}", caseId);
			legalCase.initiateLlmAnalysis("document_analysis");
			legalCaseRepository.save(legalCase);
			return true;
		}

		return false;
	}

	/**
	 * 检查案例是否应该进行LLM分析 Check if a case should undergo LLM analysis
	 */
	@Override
	public boolean shouldPerformAnalysis(LegalCase legalCase) {
		// 检查是否有完成的文档
		if (!legalCase.hasCompletedDocumentsForAnalysis()) {
			log.debug("Case {} has no completed documents for analysis", legalCase.getId());
			return false;
		}

		// 检查上次分析时间
		LocalDateTime lastAnalysisTime = getLastAnalysisTime(legalCase.getId());

		// 如果没有进行过分析，或者距离上次分析已经超过规定的间隔时间
		if (lastAnalysisTime == null) {
			log.debug("Case {} has never been analyzed before", legalCase.getId());
			return true;
		}

		boolean shouldAnalyze = LocalDateTime.now().minusHours(MIN_ANALYSIS_INTERVAL_HOURS).isAfter(lastAnalysisTime);
		log.debug("Case {} last analyzed at {}. Should analyze now: {}", legalCase.getId(), lastAnalysisTime,
				shouldAnalyze);

		return shouldAnalyze;
	}

	/**
	 * 获取上次LLM分析的时间 Get the time of the last LLM analysis
	 */
	@Override
	public LocalDateTime getLastAnalysisTime(String caseId) {
		// 从事件日志中查询最后一次LLM分析事件的时间
		Optional<LocalDateTime> lastAnalysisTime = eventLogRepository.findLastEventTimeByTypeAndCaseId(caseId,
				LLM_ANALYSIS_INITIATED_EVENT);

		return lastAnalysisTime.orElse(null);
	}

}