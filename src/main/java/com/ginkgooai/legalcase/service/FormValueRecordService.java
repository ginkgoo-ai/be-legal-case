package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.dto.FormValueRecordDTO;

import java.util.List;
import java.util.Map;

/**
 * 表单值记录服务接口 Service interface for form value records
 */
public interface FormValueRecordService {

	/**
	 * 记录表单值 Record form values
	 * @param caseId 案例ID / case ID
	 * @param formId 表单ID / form ID
	 * @param formName 表单名称 / form name
	 * @param pageId 页面ID / page ID
	 * @param pageName 页面名称 / page name
	 * @param formValues 表单值 / form values
	 * @param userId 用户ID / user ID
	 * @return 记录的表单值 / recorded form values
	 */
	FormValueRecordDTO recordFormValues(String caseId, String formId, String formName, String pageId, String pageName,
			Map<String, Object> formValues, String userId);

	/**
	 * 记录单个表单输入值 Record a single form input value
	 * @param caseId 案例ID / case ID
	 * @param formId 表单ID / form ID
	 * @param formName 表单名称 / form name
	 * @param pageId 页面ID / page ID
	 * @param pageName 页面名称 / page name
	 * @param inputId 输入元素ID / input element ID
	 * @param inputType 输入类型 / input type
	 * @param inputValue 输入值 / input value
	 * @param userId 用户ID / user ID
	 * @return 记录的表单值 / recorded form values
	 */
	FormValueRecordDTO recordInputValue(String caseId, String formId, String formName, String pageId, String pageName,
			String inputId, String inputType, String inputValue, String userId);

	/**
	 * 获取案例的所有表单值记录 Get all form value records for a case
	 * @param caseId 案例ID / case ID
	 * @return 表单值记录列表 / list of form value records
	 */
	List<FormValueRecordDTO> getAllFormValueRecords(String caseId);

	/**
	 * 获取特定表单的所有值记录 Get all form value records for a specific form
	 * @param caseId 案例ID / case ID
	 * @param formId 表单ID / form ID
	 * @return 表单值记录列表 / list of form value records
	 */
	List<FormValueRecordDTO> getFormValueRecords(String caseId, String formId);

	/**
	 * 清除特定表单的所有值记录 Clear all form value records for a specific form
	 * @param caseId 案例ID / case ID
	 * @param formId 表单ID / form ID
	 */
	void clearFormValueRecords(String caseId, String formId);

	/**
	 * 重放表单值记录 Replay form value records
	 * @param caseId 案例ID / case ID
	 * @param formId 表单ID / form ID (可选，如果为空则重放所有表单 / optional, replay all forms if null)
	 * @return 重放的结果 / result of replay
	 */
	Map<String, Object> replayFormValueRecords(String caseId, String formId);

}