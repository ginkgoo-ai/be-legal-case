package com.ginkgooai.legalcase.domain.event;

import com.ginkgooai.legalcase.domain.BaseAuditableEntity;
import com.ginkgooai.legalcase.domain.LegalCase;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLog extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@ManyToOne
	@JoinColumn(name = "case_id")
	private LegalCase legalCase;

	@Column(nullable = false)
	private String eventId;

	@Column(nullable = false)
	private String eventType;

	@Column(nullable = false)
	private LocalDateTime occurredAt;

	@Column(name = "event_data", nullable = false)
	@Type(JsonType.class)
	private String eventData;

}