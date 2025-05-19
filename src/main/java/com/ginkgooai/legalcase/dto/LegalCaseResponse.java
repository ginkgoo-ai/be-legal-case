package com.ginkgooai.legalcase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ginkgooai.legalcase.domain.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalCaseResponse {

	private String id;

	private String title;

	private String description;

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

	private List<CaseDocumentResponse> documents;
	
	private int documentsCount;

	private int eventsCount;

}