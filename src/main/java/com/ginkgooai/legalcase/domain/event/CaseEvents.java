package com.ginkgooai.legalcase.domain.event;

import com.ginkgooai.legalcase.domain.CaseStatus;
import com.ginkgooai.legalcase.domain.LegalCase;
import lombok.Getter;

import java.util.Map;

/**
 * Case-related domain events
 */
public class CaseEvents {

	/**
	 * Event fired when a new case is created
	 */
	@Getter
	public static class CaseCreatedEvent extends BaseDomainEvent {

		private final String caseId;

		private final String profileId;

		private final String caseTitle;

		public CaseCreatedEvent(String caseId, String profileId, String caseTitle) {
			super(caseId);
			this.caseId = caseId;
			this.profileId = profileId;
			this.caseTitle = caseTitle;
		}

	}

	/**
	 * Event fired when document is marked as complete
	 */
	@Getter
	public static class DocumentCompletedEvent extends BaseDomainEvent {

		private final String caseId;

		private final String documentId;

		private final String documentName;

		public DocumentCompletedEvent(String caseId, String documentId, String documentName) {
			super(caseId);
			this.caseId = caseId;
			this.documentId = documentId;
			this.documentName = documentName;
		}

	}

	/**
	 * Event fired when questionnaire is completed
	 */
	@Getter
	public static class QuestionnaireCompletedEvent extends BaseDomainEvent {

		private final String caseId;

		private final String questionnaireId;

		private final String questionnaireName;

		public QuestionnaireCompletedEvent(String caseId, String questionnaireId, String questionnaireName) {
			super(caseId);
			this.caseId = caseId;
			this.questionnaireId = questionnaireId;
			this.questionnaireName = questionnaireName;
		}

	}

	/**
	 * Event fired when a form value is recorded (for replay purposes)
	 */
	@Getter
	public static class FormValueRecordedEvent extends BaseDomainEvent {

		private final String caseId;

		private final String formId;

		private final String formName;

		private final String pageId;

		private final String pageName;

		private final String inputId;

		private final String inputType;

		private final String inputValue;

		private final Map<String, Object> formValues;

		public FormValueRecordedEvent(String caseId, String formId, String formName, String pageId, String pageName,
				String inputId, String inputType, String inputValue, Map<String, Object> formValues) {
			super(caseId);
			this.caseId = caseId;
			this.formId = formId;
			this.formName = formName;
			this.pageId = pageId;
			this.pageName = pageName;
			this.inputId = inputId;
			this.inputType = inputType;
			this.inputValue = inputValue;
			this.formValues = formValues;
		}

		// 兼容旧构造函数，不包括inputId、inputType和inputValue的情况
		public FormValueRecordedEvent(String caseId, String formId, String formName, String pageId, String pageName,
				Map<String, Object> formValues) {
			this(caseId, formId, formName, pageId, pageName, null, null, null, formValues);
		}

	}

	/**
	 * Event fired when LLM analysis is initiated
	 */
	@Getter
	public static class LlmAnalysisInitiatedEvent extends BaseDomainEvent {

		private final String caseId;

		private final String analysisType;

		public LlmAnalysisInitiatedEvent(String caseId, String analysisType) {
			super(caseId);
			this.caseId = caseId;
			this.analysisType = analysisType;
		}

	}

	/**
	 * Event fired when LLM analysis is completed
	 */
	@Getter
	public static class LlmAnalysisCompletedEvent extends BaseDomainEvent {

		private final String caseId;

		private final String analysisType;

		private final boolean successful;

		private final String resultSummary;

		public LlmAnalysisCompletedEvent(String caseId, String analysisType, boolean successful, String resultSummary) {
			super(caseId);
			this.caseId = caseId;
			this.analysisType = analysisType;
			this.successful = successful;
			this.resultSummary = resultSummary;
		}

	}

	/**
	 * Event fired when all documentation is complete
	 */
	@Getter
	public static class DocumentationCompleteEvent extends BaseDomainEvent {

		private final String caseId;

		public DocumentationCompleteEvent(String caseId) {
			super(caseId);
			this.caseId = caseId;
		}

	}

	/**
	 * Event fired when auto-filling is initiated
	 */
	@Getter
	public static class AutoFillingInitiatedEvent extends BaseDomainEvent {

		private final String caseId;

		public AutoFillingInitiatedEvent(String caseId) {
			super(caseId);
			this.caseId = caseId;
		}

	}

	@Getter
	public static class AutoFillingCompletedEvent extends BaseDomainEvent {

		private final String caseId;

		public AutoFillingCompletedEvent(String caseId) {
			super(caseId);
			this.caseId = caseId;
		}

	}

	/**
	 * Event fired when case is put on hold
	 */
	@Getter
	public static class CasePutOnHoldEvent extends BaseDomainEvent {

		private final String caseId;

		private final String reason;

		public CasePutOnHoldEvent(String caseId, String reason) {
			super(caseId);
			this.caseId = caseId;
			this.reason = reason;
		}

	}

	/**
	 * Event fired when case is resumed from hold
	 */
	@Getter
	public static class CaseResumedEvent extends BaseDomainEvent {

		private final String caseId;

		public CaseResumedEvent(String caseId) {
			super(caseId);
			this.caseId = caseId;
		}

	}

	/**
	 * Event fired when case is submitted for final review
	 */
	@Getter
	public static class CaseSubmittedEvent extends BaseDomainEvent {

		private final String caseId;

		private final String submittedBy;

		public CaseSubmittedEvent(String caseId, String submittedBy) {
			super(caseId);
			this.caseId = caseId;
			this.submittedBy = submittedBy;
		}

	}

	/**
	 * Event fired when case is approved
	 */
	@Getter
	public static class CaseApprovedEvent extends BaseDomainEvent {

		private final String caseId;

		private final String approvedBy;

		private final String comments;

		public CaseApprovedEvent(String caseId, String approvedBy, String comments) {
			super(caseId);
			this.caseId = caseId;
			this.approvedBy = approvedBy;
			this.comments = comments;
		}

	}

	/**
	 * Event fired when case is denied
	 */
	@Getter
	public static class CaseDeniedEvent extends BaseDomainEvent {

		private final String caseId;

		private final String deniedBy;

		private final String reason;

		public CaseDeniedEvent(String caseId, String deniedBy, String reason) {
			super(caseId);
			this.caseId = caseId;
			this.deniedBy = deniedBy;
			this.reason = reason;
		}

	}

}