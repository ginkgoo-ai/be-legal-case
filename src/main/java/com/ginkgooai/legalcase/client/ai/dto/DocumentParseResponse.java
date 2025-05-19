package com.ginkgooai.legalcase.client.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO from document parsing service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParseResponse {

	/**
	 * P60 data extracted from documents
	 */
	@JsonProperty("p60")
	private Map<String, Object> p60;

	/**
	 * Parents information extracted from documents
	 */
	@JsonProperty("parents_info")
	private Map<String, Object> parentsInfo;

	/**
	 * Passport data extracted from documents
	 */
	@JsonProperty("passport")
	private Map<String, Object> passport;

	/**
	 * Referee and identity data extracted from documents
	 */
	@JsonProperty("referee_and_identity")
	private Map<String, Object> refereeAndIdentity;

	/**
	 * Referee information extracted from documents
	 */
	@JsonProperty("referee_info")
	private Map<String, Object> refereeInfo;

	/**
	 * Utility bill data extracted from documents
	 */
	@JsonProperty("utility_bill")
	private Map<String, Object> utilityBill;

}