package com.ginkgooai.legalcase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a case document
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseDocumentRequest {

	@NotBlank(message = "Document title cannot be empty")
	@Size(max = 255, message = "Title length cannot exceed 255 characters")
	private String title;

	@Size(max = 1000, message = "Description length cannot exceed 1000 characters")
	private String description;

	private String documentType;

}