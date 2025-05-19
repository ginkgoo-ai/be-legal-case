package com.ginkgooai.legalcase.domain;

import com.ginkgooai.legalcase.domain.event.CaseEvents;
import com.ginkgooai.legalcase.domain.event.DomainEvent;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Legal case aggregate root
 */
@Entity
@Table(name = "legal_cases")
@Data
@Slf4j
@NoArgsConstructor
public class LegalCase extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String title;

	@Column(length = 1000)
	private String description;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "profile_id")
	private String profileId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CaseStatus status = CaseStatus.DRAFT;

	@OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CaseDocument> documents = new ArrayList<>();

	@Transient
	private List<DomainEvent> domainEvents = new CopyOnWriteArrayList<>();

	/**
	 * Register a domain event
	 * @param event Domain event
	 */
	public void registerEvent(DomainEvent event) {
		this.domainEvents.add(event);
	}

	/**
	 * Get all registered domain events and clear the list
	 * @return List of registered domain events
	 */
	public List<DomainEvent> getAndClearDomainEvents() {
		List<DomainEvent> events = new ArrayList<>(this.domainEvents);
		this.domainEvents.clear();
		return events;
	}

	/**
	 * Create a new case
	 * @param profileId Profile ID
	 * @param title Case title
	 * @param description Case description
	 */
	public LegalCase(String clientId, String profileId, String title, String description) {
		this.clientId = clientId;
		this.profileId = profileId;
		this.title = title;
		this.description = description;
		this.status = CaseStatus.DOCUMENTATION_IN_PROGRESS;

		registerEvent(new CaseEvents.CaseCreatedEvent(this.id, profileId, title));
	}

	/**
	 * Add a document to the case
	 * @param document Case document
	 */
	public void addDocument(CaseDocument document) {
		document.setLegalCase(this);
		this.documents.add(document);
	}

	/**
	 * Get all questionnaire documents in this case
	 * @return List of questionnaire documents
	 */
	public List<QuestionnaireDocument> getQuestionnaireDocuments() {
		return this.documents.stream()
			.filter(doc -> doc instanceof QuestionnaireDocument)
			.map(doc -> (QuestionnaireDocument) doc)
			.collect(Collectors.toList());
	}

	public List<ProfileDocument> getProfileDocuments() {
		return this.documents.stream()
			.filter(doc -> doc instanceof ProfileDocument)
			.map(doc -> (ProfileDocument) doc)
			.collect(Collectors.toList());
	}

	/**
	 * Get all supporting documents in this case
	 * @return List of supporting documents
	 */
	public List<SupportingDocument> getSupportingDocuments() {
		return this.documents.stream()
			.filter(doc -> doc instanceof SupportingDocument)
			.map(doc -> (SupportingDocument) doc)
			.collect(Collectors.toList());
	}

	public void addProfileDocument(ProfileDocument document) {
		document.setLegalCase(this);
		document.setDocumentCategory(CaseDocument.DocumentCategory.PROFILE);
		this.documents.add(document);
	}

	public void addQuestionnaireDocument(QuestionnaireDocument document) {
		document.setLegalCase(this);
		document.setDocumentCategory(CaseDocument.DocumentCategory.QUESTIONNAIRE);
		this.documents.add(document);
	}

	/**
	 * Add a supporting document to the case
	 * @param document Supporting document
	 */
	public void addSupportingDocument(SupportingDocument document) {
		document.setLegalCase(this);
		document.setDocumentCategory(CaseDocument.DocumentCategory.SUPPORTING_DOCUMENT);
		this.documents.add(document);
	}

	/**
	 * Mark a document as complete
	 * @param documentId document ID
	 * @param documentName document name
	 */
	public void markDocumentComplete(String documentId, String documentName) {
		// Find the document and update its status
		this.documents.stream().filter(doc -> documentId.equals(doc.getId())).findFirst().ifPresent(doc -> {
			doc.setStatus(CaseDocument.DocumentStatus.COMPLETE);
			registerEvent(new CaseEvents.DocumentCompletedEvent(this.id, documentId, documentName));

			// Fire document completion event, LLM analysis will be handled by service
			// layer
		});
	}

	/**
	 * Mark a questionnaire as complete
	 * @param questionnaireId questionnaire ID
	 * @param questionnaireName questionnaire name
	 */
	public void markQuestionnaireComplete(String questionnaireId, String questionnaireName) {
		// Find the questionnaire document and update its status
		this.getQuestionnaireDocuments()
			.stream()
			.filter(doc -> questionnaireId.equals(doc.getId()))
			.findFirst()
			.ifPresent(doc -> {
				doc.setStatus(CaseDocument.DocumentStatus.COMPLETE);
				registerEvent(new CaseEvents.QuestionnaireCompletedEvent(this.id, questionnaireId, questionnaireName));

				// Fire questionnaire completion event, LLM analysis will be handled by
				// service layer
			});
	}

	/**
	 * Check if LLM analysis should be initiated based on document/questionnaire
	 * completion (only checks document status, not responsible for time-based decisions)
	 * @return whether LLM analysis should be initiated
	 */
	public boolean hasCompletedDocumentsForAnalysis() {
		return this.documents.stream().anyMatch(CaseDocument::isComplete);
	}

	/**
	 * Initiate LLM analysis
	 * @param analysisType analysis type
	 */
	public void initiateLlmAnalysis(String analysisType) {
		// Update case status
		CaseStatus previousStatus = this.status;
		this.status = CaseStatus.ANALYZING;

		// registerEvent(new CaseEvents.CaseStatusChangedEvent(
		// this.id, previousStatus, this.status, "LLM analysis initiated"));
		registerEvent(new CaseEvents.LlmAnalysisInitiatedEvent(this.id, analysisType));
	}

	/**
	 * Complete LLM analysis
	 * @param successful whether successful
	 * @param resultSummary result summary
	 */
	public void completeLlmAnalysis(boolean successful, String resultSummary) {
		// Update case status
		CaseStatus previousStatus = this.status;

		// Check if all documentation is complete
		if (isAllDocumentationComplete()) {
			this.status = CaseStatus.DOCUMENTATION_COMPLETE;
		}
		else {
			this.status = CaseStatus.DOCUMENTATION_IN_PROGRESS;
		}

		// registerEvent(new CaseEvents.CaseStatusChangedEvent(
		// this.id, previousStatus, this.status, "LLM analysis completed"));
		registerEvent(
				new CaseEvents.LlmAnalysisCompletedEvent(this.id, "document_analysis", successful, resultSummary));

		// If all documentation is complete, fire documentation completion event
		if (this.status == CaseStatus.DOCUMENTATION_COMPLETE) {
			registerEvent(new CaseEvents.DocumentationCompleteEvent(this.id));
		}
	}

	/**
	 * Check if all required documentation is complete
	 * @return whether all documentation is complete
	 */
	public boolean isAllDocumentationComplete() {
		// Check if all questionnaires are complete
		boolean allQuestionnairesComplete = this.getQuestionnaireDocuments()
			.stream()
			.allMatch(QuestionnaireDocument::isComplete);

		// Check if required supporting documents are complete
		boolean allSupportingDocsComplete = true;
		List<SupportingDocument> requiredDocs = this.getSupportingDocuments()
			.stream()
			.filter(SupportingDocument::isRequired)
			.collect(Collectors.toList());

		if (!requiredDocs.isEmpty()) {
			allSupportingDocsComplete = requiredDocs.stream().allMatch(SupportingDocument::isComplete);
		}

		return allQuestionnairesComplete && !this.getQuestionnaireDocuments().isEmpty() && allSupportingDocsComplete;
	}

	/**
	 * Initiate auto-filling
	 */
	public void initiateAutoFilling() {
		// Check if status is DOCUMENTATION_COMPLETE
		if (this.status != CaseStatus.DOCUMENTATION_COMPLETE && this.status != CaseStatus.READY_TO_FILL) {
			throw new IllegalStateException("Cannot initiate auto-filling: documentation is not complete");
		}

		CaseStatus previousStatus = this.status;
		this.status = CaseStatus.AUTO_FILLING;

		// registerEvent(new CaseEvents.CaseStatusChangedEvent(
		// this.id, previousStatus, this.status, "Auto-filling initiated"));
		registerEvent(new CaseEvents.AutoFillingInitiatedEvent(this.id));
	}

	/**
	 * Put the case on hold
	 * @param reason reason for pause
	 */
	public void putOnHold(String reason) {
		CaseStatus previousStatus = this.status;
		this.status = CaseStatus.ON_HOLD;

		registerEvent(new CaseEvents.CasePutOnHoldEvent(this.id, reason));
	}

	/**
	 * Resume the case from on-hold status
	 */
	public void resumeFromHold() {
		if (this.status != CaseStatus.ON_HOLD) {
			throw new IllegalStateException("Case is not on hold");
		}

		CaseStatus previousStatus = this.status;
		this.status = CaseStatus.AUTO_FILLING;

		registerEvent(new CaseEvents.CaseResumedEvent(this.id));
	}

	/**
	 * Complete auto-filling
	 */
	public void completeAutoFilling() {
		if (this.status != CaseStatus.AUTO_FILLING) {
			throw new IllegalStateException("Case is not in auto-filling state");
		}

		CaseStatus previousStatus = this.status;
		this.status = CaseStatus.FINAL_REVIEW;

		registerEvent(new CaseEvents.AutoFillingCompletedEvent(this.id));
	}

	/**
	 * Submit the case
	 * @param submittedBy submitted by
	 */
	public void submitCase(String submittedBy) {
		if (this.status != CaseStatus.FINAL_REVIEW) {
			throw new IllegalStateException("Case is not in final review state");
		}

		CaseStatus previousStatus = this.status;
		this.status = CaseStatus.SUBMITTED;

		registerEvent(new CaseEvents.CaseSubmittedEvent(this.id, submittedBy));
	}

	/**
	 * Approve the case
	 * @param approvedBy approved by
	 * @param comments approval comments
	 */
	public void approveCase(String approvedBy, String comments) {
		if (this.status != CaseStatus.SUBMITTED) {
			throw new IllegalStateException("Case is not in submitted state");
		}

		CaseStatus previousStatus = this.status;
		this.status = CaseStatus.APPROVED;

		registerEvent(new CaseEvents.CaseApprovedEvent(this.id, approvedBy, comments));
	}

	/**
	 * Deny the case
	 * @param deniedBy denied by
	 * @param reason denial reason
	 */
	public void denyCase(String deniedBy, String reason) {
		if (this.status != CaseStatus.SUBMITTED) {
			throw new IllegalStateException("Case is not in submitted state");
		}

		CaseStatus previousStatus = this.status;
		this.status = CaseStatus.DENIED;

		registerEvent(new CaseEvents.CaseDeniedEvent(this.id, deniedBy, reason));
	}

	/**
	 * Record form value
	 * @param formId form ID
	 * @param formName form name
	 * @param pageId page ID
	 * @param pageName page name
	 * @param inputId input ID
	 * @param inputType input type
	 * @param inputValue input value
	 * @param sequenceNumber sequence number
	 */
	public void recordFormValue(String formId, String formName, String pageId, String pageName, String inputId,
			String inputType, String inputValue) {

		Map<String, Object> formValues = new HashMap<>();
		formValues.put(inputId, inputValue);

		registerEvent(new CaseEvents.FormValueRecordedEvent(this.id, formId, formName, pageId, pageName, inputId,
				inputType, inputValue, formValues));
	}

}