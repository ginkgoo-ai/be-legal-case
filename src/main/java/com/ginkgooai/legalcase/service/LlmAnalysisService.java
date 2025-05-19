package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.domain.LegalCase;

/**
 * LLM分析服务接口 Interface for LLM analysis service
 */
public interface LlmAnalysisService {

	/**
	 * 检查并在必要时触发LLM分析 Check and trigger LLM analysis if necessary
	 * @param caseId 案例ID / case ID
	 * @return 是否触发了分析 / whether analysis was triggered
	 */
	boolean checkAndTriggerAnalysis(String caseId);

	/**
	 * 检查案例是否应该进行LLM分析 Check if a case should undergo LLM analysis
	 * @param legalCase 法律案例 / legal case
	 * @return 是否应该分析 / whether analysis should be performed
	 */
	boolean shouldPerformAnalysis(LegalCase legalCase);

	/**
	 * 获取上次LLM分析的时间 Get the time of the last LLM analysis
	 * @param caseId 案例ID / case ID
	 * @return 上次分析时间 / time of the last analysis
	 */
	java.time.LocalDateTime getLastAnalysisTime(String caseId);

}