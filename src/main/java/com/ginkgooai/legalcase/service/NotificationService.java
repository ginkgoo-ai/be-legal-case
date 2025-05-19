package com.ginkgooai.legalcase.service;

/**
 * Service for sending notifications
 */
public interface NotificationService {

	/**
	 * Notify user about case creation
	 * @param profileId the profile ID of the user
	 * @param caseId the case ID
	 * @param caseTitle the case title
	 */
	void notifyUserAboutCaseCreation(String profileId, String caseId, String caseTitle);

	/**
	 * Notify about case status change
	 * @param caseId the case ID
	 * @param previousStatus the previous status
	 * @param newStatus the new status
	 * @param reason the reason for the change
	 */
	void notifyAboutStatusChange(String caseId, String previousStatus, String newStatus, String reason);

	/**
	 * Notify about analysis completion
	 * @param caseId the case ID
	 * @param successful whether the analysis was successful
	 * @param resultSummary the result summary
	 */
	void notifyAboutAnalysisCompletion(String caseId, boolean successful, String resultSummary);

	/**
	 * Notify that a case is ready for review
	 * @param caseId the case ID
	 */
	void notifyAboutReviewReady(String caseId);

	/**
	 * Notify that a case is on hold
	 * @param caseId the case ID
	 * @param reason the reason
	 */
	void notifyCaseOnHold(String caseId, String reason);

	/**
	 * Notify that a case is resumed
	 * @param caseId the case ID
	 */
	void notifyCaseResumed(String caseId);

	/**
	 * Notify approvers about case submission
	 * @param caseId the case ID
	 * @param submittedBy who submitted the case
	 */
	void notifyApproversAboutSubmission(String caseId, String submittedBy);

	/**
	 * Notify that a case is approved
	 * @param caseId the case ID
	 * @param approvedBy who approved the case
	 * @param comments approval comments
	 */
	void notifyCaseApproved(String caseId, String approvedBy, String comments);

	/**
	 * Notify that a case is denied
	 * @param caseId the case ID
	 * @param deniedBy who denied the case
	 * @param reason denial reason
	 */
	void notifyCaseDenied(String caseId, String deniedBy, String reason);

}