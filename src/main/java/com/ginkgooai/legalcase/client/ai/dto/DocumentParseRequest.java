package com.ginkgooai.legalcase.client.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for document parsing service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParseRequest {

	/**
	 * List of document file URLs to parse
	 */
	@JsonProperty("doc_urls")
	private List<String> fileUrls;

}