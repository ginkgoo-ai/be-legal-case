package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.dto.EventLogResponse;

import java.util.List;

/**
 * 事件日志服务接口 Event log service interface
 */
public interface EventLogService {

	/**
	 * 获取特定案例的所有事件日志 Get all event logs for a specific case
	 * @param caseId 案例ID / case ID
	 * @return 事件日志响应列表 / list of event log responses
	 */
	List<EventLogResponse> getEventLogsForCase(String caseId);

	/**
	 * 获取特定案例的特定类型事件日志 Get event logs for a specific case and event type
	 * @param caseId 案例ID / case ID
	 * @param eventType 事件类型 / event type
	 * @return 事件日志响应列表 / list of event log responses
	 */
	List<EventLogResponse> getEventLogsForCaseAndType(String caseId, String eventType);

}