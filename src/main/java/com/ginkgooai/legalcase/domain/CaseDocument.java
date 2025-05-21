package com.ginkgooai.legalcase.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "case_documents")
@Getter
@Setter
@ToString(exclude = "legalCase")
@EqualsAndHashCode(callSuper = true, exclude = "legalCase")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "document_category", discriminatorType = DiscriminatorType.STRING)
public class CaseDocument extends BaseLogicalDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String title;

	@Column(length = 1000)
	private String description;

	@Column(nullable = false)
	private String filePath;

	private String fileType;

	private Long fileSize;

	private String storageId;

	@ManyToOne
	@JoinColumn(name = "case_id", nullable = false)
	private LegalCase legalCase;

	@Enumerated(EnumType.STRING)
	private DocumentType documentType;

	@Enumerated(EnumType.STRING)
	private DocumentStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "category")
	private DocumentCategory documentCategory;

	@Type(JsonType.class)
	@Column(columnDefinition = "jsonb")
	private String metadataJson;

	@Transient
	private Map<String, Object> metadata = new HashMap<>();

	@Transient
	private String downloadUrl;

	/**
	 * Document category enumeration - corresponding to the three types required by users
	 */
	public enum DocumentCategory {

		QUESTIONNAIRE("Questionnaire"), PROFILE("Profile Information"), SUPPORTING_DOCUMENT("Supporting Document");

		private final String displayName;

		DocumentCategory(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}

	}

	/**
	 * Document status enumeration
	 */
	public enum DocumentStatus {

		PENDING("Pending"), INCOMPLETE("Incomplete"), COMPLETE("Complete"), REJECTED("Rejected"), EXPIRED("Expired");

		private final String displayName;

		DocumentStatus(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}

	}

	/**
	 * Check if the document is complete
	 */
	public boolean isComplete() {
		return DocumentStatus.COMPLETE.equals(this.status);
	}

}