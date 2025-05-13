package com.ginkgooai.core.legalcase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ginkgooai.core.legalcase.domain.LegalCase.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalCaseResponse {

	private String id;

	private String title;

	private String description;

	private String caseNumber;

	private String profileId;

	private String clientId;

	private CaseStatus status;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime startDate;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime endDate;

	private String clientName;

	private String profileName;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime updatedAt;

	private int documentsCount;

	private int notesCount;

	private int eventsCount;

}