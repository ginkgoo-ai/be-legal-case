package com.ginkgooai.core.legalcase.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "case_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseDocument {

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "case_id", nullable = false)
	private LegalCase legalCase;

	@Enumerated(EnumType.STRING)
	private DocumentType documentType;

	private String createdBy;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@Transient
	private String downloadUrl;

	/**
	 * Document type enumeration
	 */
	public enum DocumentType {

		CONTRACT, // Contract
		EVIDENCE, // Evidence
		COURT_FILING, // Court Filing
		CORRESPONDENCE, // Correspondence
		NOTES, // Notes
		OTHER // Other

	}

}