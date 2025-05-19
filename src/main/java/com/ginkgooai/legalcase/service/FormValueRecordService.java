package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.dto.FormValueRecordDTO;

import java.util.List;
import java.util.Map;

/**
 * Service for managing form value records
 */
public interface FormValueRecordService {

	/**
	 * Record form values
	 * @param caseId case ID
	 * @param formId form ID
	 * @param formName form name
	 * @param pageId page ID
	 * @param pageName page name
	 * @param formValues form values
	 * @param userId user ID
	 * @return recorded form values
	 */
	FormValueRecordDTO recordFormValues(String caseId, String formId, String formName, String pageId, String pageName,
			Map<String, Object> formValues, String userId);

	/**
	 * Record a single form input value
	 * @param caseId case ID
	 * @param formId form ID
	 * @param formName form name
	 * @param pageId page ID
	 * @param pageName page name
	 * @param inputId input element ID
	 * @param inputType input type
	 * @param inputValue input value
	 * @param userId user ID
	 * @return recorded form values
	 */
	FormValueRecordDTO recordInputValue(String caseId, String formId, String formName, String pageId, String pageName,
			String inputId, String inputType, String inputValue, String userId);

	/**
	 * Get all form value records for a case
	 * @param caseId case ID
	 * @return list of form value records
	 */
	List<FormValueRecordDTO> getAllFormValueRecords(String caseId);

	/**
	 * Get all form value records for a specific form
	 * @param caseId case ID
	 * @param formId form ID
	 * @return list of form value records
	 */
	List<FormValueRecordDTO> getFormValueRecords(String caseId, String formId);

	/**
	 * Clear all form value records for a specific form
	 * @param caseId case ID
	 * @param formId form ID
	 */
	void clearFormValueRecords(String caseId, String formId);

	/**
	 * Replay form value records
	 * @param caseId case ID
	 * @param formId form ID (optional, replay all forms if null)
	 * @return result of replay
	 */
	Map<String, Object> replayFormValueRecords(String caseId, String formId);

}