package com.ginkgooai.legalcase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for case creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseCreationRequest {

	private String title;

	private String description;

	private String profileId;

	private String clientId;

}