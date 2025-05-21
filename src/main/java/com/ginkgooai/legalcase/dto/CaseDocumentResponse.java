package com.ginkgooai.legalcase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ginkgooai.legalcase.domain.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseDocumentResponse {

	private String id;

	private String title;

	private String description;

	private String filePath;

	private String fileType;

	private Long fileSize;

	private String storageId;

	private String caseId;

	private DocumentType documentType;

	private String downloadUrl;

	private String metadataJson;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime updatedAt;

	private String createdBy;

}