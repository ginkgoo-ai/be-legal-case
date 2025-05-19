package com.ginkgooai.legalcase.domain;

import com.ginkgooai.legalcase.domain.event.CaseEvents;
import com.ginkgooai.legalcase.domain.event.DomainEvent;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.*;
import java.time.LocalDateTime;
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
	 * 将文档标记为完成 Mark a document as complete
	 * @param documentId 文档ID / document ID
	 * @param documentName 文档名称 / document name
	 */
	public void markDocumentComplete(String documentId, String documentName) {
		// 查找文档并更新其状态
		this.documents.stream().filter(doc -> documentId.equals(doc.getId())).findFirst().ifPresent(doc -> {
			doc.setStatus(CaseDocument.DocumentStatus.COMPLETE);
			registerEvent(new CaseEvents.DocumentCompletedEvent(this.id, documentId, documentName));

			// 发出文档完成事件，后续的LLM分析判断由服务层处理
		});
	}

	/**
	 * 将问卷标记为完成 Mark a questionnaire as complete
	 * @param questionnaireId 问卷ID / questionnaire ID
	 * @param questionnaireName 问卷名称 / questionnaire name
	 */
	public void markQuestionnaireComplete(String questionnaireId, String questionnaireName) {
		// 查找问卷文档并更新其状态
		this.getQuestionnaireDocuments()
			.stream()
			.filter(doc -> questionnaireId.equals(doc.getId()))
			.findFirst()
			.ifPresent(doc -> {
				doc.setStatus(CaseDocument.DocumentStatus.COMPLETE);
				registerEvent(new CaseEvents.QuestionnaireCompletedEvent(this.id, questionnaireId, questionnaireName));

				// 发出问卷完成事件，后续的LLM分析判断由服务层处理
			});
	}

	/**
	 * 检查是否应该触发LLM分析（仅检查文档完成状态，不负责时间判断） Check if LLM analysis should be initiated based on
	 * document/questionnaire completion (only checks document status, not responsible for
	 * time-based decisions)
	 * @return 是否应该触发LLM分析 / whether LLM analysis should be initiated
	 */
	public boolean hasCompletedDocumentsForAnalysis() {
		return this.documents.stream().anyMatch(CaseDocument::isComplete);
	}

	/**
	 * 开始LLM分析 Initiate LLM analysis
	 * @param analysisType 分析类型 / analysis type
	 */
	public void initiateLlmAnalysis(String analysisType) {
		// 更新案例状态
		CaseStatus previousStatus = this.status;
		this.status = CaseStatus.ANALYZING;

		// registerEvent(new CaseEvents.CaseStatusChangedEvent(
		// this.id, previousStatus, this.status, "LLM analysis initiated"));
		registerEvent(new CaseEvents.LlmAnalysisInitiatedEvent(this.id, analysisType));
	}

	/**
	 * 完成LLM分析 Complete LLM analysis
	 * @param successful 是否成功 / whether successful
	 * @param resultSummary 结果摘要 / result summary
	 */
	public void completeLlmAnalysis(boolean successful, String resultSummary) {
		// 更新案例状态
		CaseStatus previousStatus = this.status;

		// 检查所有文档是否已完成
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

		// 如果文档已全部完成，发出文档完成事件
		if (this.status == CaseStatus.DOCUMENTATION_COMPLETE) {
			registerEvent(new CaseEvents.DocumentationCompleteEvent(this.id));
		}
	}

	/**
	 * 判断所有必需的文档是否已完成 Check if all required documentation is complete
	 * @return 是否所有文档已完成 / whether all documentation is complete
	 */
	public boolean isAllDocumentationComplete() {
		// 检查所有问卷是否完成
		boolean allQuestionnairesComplete = this.getQuestionnaireDocuments()
			.stream()
			.allMatch(QuestionnaireDocument::isComplete);

		// 检查必要的支持文档是否完成
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
	 * 开始自动填充 Initiate auto-filling
	 */
	public void initiateAutoFilling() {
		// 检查状态是否为DOCUMENTATION_COMPLETE
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
	 * 暂停案例处理 Put the case on hold
	 * @param reason 暂停原因 / reason for pause
	 */
	public void putOnHold(String reason) {
		CaseStatus previousStatus = this.status;
		this.status = CaseStatus.ON_HOLD;

		registerEvent(new CaseEvents.CasePutOnHoldEvent(this.id, reason));
	}

	/**
	 * 恢复案例处理 Resume the case from on-hold status
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
	 * 完成自动填充 Complete auto-filling
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
	 * 提交案例 Submit the case
	 * @param submittedBy 提交人 / submitted by
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
	 * 批准案例 Approve the case
	 * @param approvedBy 批准人 / approved by
	 * @param comments 批准意见 / approval comments
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
	 * 拒绝案例 Deny the case
	 * @param deniedBy 拒绝人 / denied by
	 * @param reason 拒绝原因 / denial reason
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
	 * 记录表单值 Record form value
	 * @param formId 表单ID / form ID
	 * @param formName 表单名称 / form name
	 * @param pageId 页面ID / page ID
	 * @param pageName 页面名称 / page name
	 * @param inputId 输入ID / input ID
	 * @param inputType 输入类型 / input type
	 * @param inputValue 输入值 / input value
	 * @param sequenceNumber 序列号 / sequence number
	 */
	public void recordFormValue(String formId, String formName, String pageId, String pageName, String inputId,
			String inputType, String inputValue) {

		Map<String, Object> formValues = new HashMap<>();
		formValues.put(inputId, inputValue);

		registerEvent(new CaseEvents.FormValueRecordedEvent(this.id, formId, formName, pageId, pageName, inputId,
				inputType, inputValue, formValues));
	}

}