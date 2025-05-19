package com.ginkgooai.legalcase.client.ai.dto;

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
	 * Status of the parsing operation
	 */
	private String status;

	/**
	 * Any message from the parsing service
	 */
	private String message;

	/**
	 * Extracted profile data from documents
	 */
	private ProfileData profileData;

	/**
	 * Contains structured profile data extracted from documents
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProfileData {

		/**
		 * P60 data extracted from documents
		 */
		private Map<String, Object> p60Data;

		/**
		 * Parents information extracted from documents
		 */
		private Map<String, Object> parentsInfo;

		/**
		 * Passport data extracted from documents
		 */
		private Map<String, Object> passportData;

		/**
		 * Referee and identity data extracted from documents
		 */
		private Map<String, Object> refereeAndIdentityData;

		/**
		 * Referee information extracted from documents
		 */
		private Map<String, Object> refereeInfo;

		/**
		 * Utility bill data extracted from documents
		 */
		private Map<String, Object> utilityBillData;

	}

}