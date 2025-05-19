package com.ginkgooai.legalcase.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Empty implementation of NotificationService
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

	@Override
	public void notifyUserAboutCaseCreation(String profileId, String caseId, String caseTitle) {
		log.info("Empty implementation - notifyUserAboutCaseCreation: profileId={}, caseId={}, caseTitle={}", profileId,
				caseId, caseTitle);
	}

	@Override
	public void notifyAboutStatusChange(String caseId, String previousStatus, String newStatus, String reason) {
		log.info(
				"Empty implementation - notifyAboutStatusChange: caseId={}, previousStatus={}, newStatus={}, reason={}",
				caseId, previousStatus, newStatus, reason);
	}

	@Override
	public void notifyAboutAnalysisCompletion(String caseId, boolean successful, String resultSummary) {
		log.info("Empty implementation - notifyAboutAnalysisCompletion: caseId={}, successful={}, resultSummary={}",
				caseId, successful, resultSummary);
	}

	@Override
	public void notifyAboutReviewReady(String caseId) {
		log.info("Empty implementation - notifyAboutReviewReady: caseId={}", caseId);
	}

	@Override
	public void notifyCaseOnHold(String caseId, String reason) {
		log.info("Empty implementation - notifyCaseOnHold: caseId={}, reason={}", caseId, reason);
	}

	@Override
	public void notifyCaseResumed(String caseId) {
		log.info("Empty implementation - notifyCaseResumed: caseId={}", caseId);
	}

	@Override
	public void notifyApproversAboutSubmission(String caseId, String submittedBy) {
		log.info("Empty implementation - notifyApproversAboutSubmission: caseId={}, submittedBy={}", caseId,
				submittedBy);
	}

	@Override
	public void notifyCaseApproved(String caseId, String approvedBy, String comments) {
		log.info("Empty implementation - notifyCaseApproved: caseId={}, approvedBy={}, comments={}", caseId, approvedBy,
				comments);
	}

	@Override
	public void notifyCaseDenied(String caseId, String deniedBy, String reason) {
		log.info("Empty implementation - notifyCaseDenied: caseId={}, deniedBy={}, reason={}", caseId, deniedBy,
				reason);
	}

}