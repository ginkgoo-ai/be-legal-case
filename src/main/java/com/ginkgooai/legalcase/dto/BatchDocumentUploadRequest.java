package com.ginkgooai.legalcase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for batch document upload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDocumentUploadRequest {

	/**
	 * List of storage IDs of the uploaded files
	 */
	private List<String> storageIds;

}