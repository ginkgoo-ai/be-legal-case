package com.ginkgooai.legalcase.domain;

/**
 * Document type enumeration
 */
public enum DocumentType {

	PASSPORT("Passport"), UTILITY_BILL("Utility Bill"), P60("P60"), REFEREE_INFO("Referee Information"),
	REFEREE_AND_IDENTITY("Referee and Identity"), PARENTS_INFO("Parents Information"), QUESTIONNAIRE("Questionnaire"),
	OTHER("Other");

	private final String displayName;

	DocumentType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

}
