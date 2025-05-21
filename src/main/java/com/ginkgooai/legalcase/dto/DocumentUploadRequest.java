package com.ginkgooai.legalcase.dto;

import com.ginkgooai.legalcase.domain.CaseDocument;
import com.ginkgooai.legalcase.domain.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for document upload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {

	/**
	 * Storage ID from the storage service
	 */
	private String storageId;

	/**
	 * Type of document
	 */
	private DocumentType documentType;

	/**
	 * Category of document (questionnaire, profile, or supporting document)
	 */
	private CaseDocument.DocumentCategory documentCategory;

	/**
	 * Additional metadata for the document
	 */
	private Map<String, Object> metadata;

}