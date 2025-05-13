package com.ginkgooai.core.legalcase.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "legal_cases")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalCase {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String title;

	@Column(length = 2000)
	private String description;

	@Column(unique = true, nullable = false)
	private String caseNumber;

	@Column(nullable = false)
	private String profileId;

	@Column(nullable = false)
	private String clientId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CaseStatus status;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CaseDocument> documents = new ArrayList<>();

	@OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CaseNote> notes = new ArrayList<>();

	@OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CaseEvent> events = new ArrayList<>();

	/**
	 * Case status enumeration
	 */
	public enum CaseStatus {

		OPEN, // Open
		IN_PROGRESS, // In Progress
		PENDING, // Pending
		CLOSED // Closed

	}

}