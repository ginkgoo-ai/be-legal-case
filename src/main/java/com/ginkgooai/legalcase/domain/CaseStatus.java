package com.ginkgooai.legalcase.domain;

public enum CaseStatus {

	DRAFT("Draft"),

	DOCUMENTATION_IN_PROGRESS("Documentation In Progress"),

	ANALYZING("Analyzing"),

	DOCUMENTATION_COMPLETE("Documentation Complete"),

	REVIEW_PENDING("Review Pending"),

	READY_TO_FILL("Ready to Fill"),

	AUTO_FILLING("Auto-Filling"),

	ON_HOLD("On Hold"),

	FINAL_REVIEW("Final Review"),

	SUBMITTED("Submitted"),

	APPROVED("Approved"),

	DENIED("Denied");

	private final String displayName;

	CaseStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}

}