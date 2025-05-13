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
@Table(name = "case_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String title;

	@Column(length = 1000)
	private String description;

	@Column(nullable = false)
	private LocalDateTime eventTime;

	private String location;

	@Enumerated(EnumType.STRING)
	private EventType eventType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "case_id", nullable = false)
	private LegalCase legalCase;

	private String createdBy;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	/**
	 * Event type enumeration
	 */
	public enum EventType {

		COURT_HEARING, // Court Hearing
		CLIENT_MEETING, // Client Meeting
		DEADLINE, // Deadline
		FILING, // Filing
		OTHER // Other

	}

}