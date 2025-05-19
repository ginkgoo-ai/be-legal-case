package com.ginkgooai.legalcase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for document status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusResponse {

	/**
	 * Document ID
	 */
	private String documentId;

	/**
	 * Document title
	 */
	private String title;

	/**
	 * Document status (PENDING, INCOMPLETE, COMPLETE, REJECTED, EXPIRED)
	 */
	private String status;

	/**
	 * Document type (IDENTITY, FINANCIAL, etc.)
	 */
	private String documentType;

	/**
	 * Document category (QUESTIONNAIRE, PROFILE, SUPPORTING_DOCUMENT)
	 */
	private String documentCategory;

	/**
	 * Whether the document is complete
	 */
	private boolean isComplete;

	/**
	 * Public URL to access the document (if available)
	 */
	private String publicUrl;

	/**
	 * Error message (if applicable)
	 */
	private String errorMessage;

}