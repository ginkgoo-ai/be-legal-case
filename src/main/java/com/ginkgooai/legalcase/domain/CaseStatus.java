package com.ginkgooai.legalcase.domain;

/**
 * 案例状态枚举 Enum representing the possible status values for a legal case
 */
public enum CaseStatus {

	// 初始状态
	DRAFT("Draft"),

	// 文档填写状态
	DOCUMENTATION_IN_PROGRESS("Documentation In Progress"),

	// 分析状态
	ANALYZING("Analyzing"),

	// 文档完成状态
	DOCUMENTATION_COMPLETE("Documentation Complete"),

	// 待审阅状态
	REVIEW_PENDING("Review Pending"),

	// 待填充状态
	READY_TO_FILL("Ready to Fill"),

	// 自动填充状态
	AUTO_FILLING("Auto-Filling"),

	// 暂停状态
	ON_HOLD("On Hold"),

	// 最终审阅状态
	FINAL_REVIEW("Final Review"),

	// 已提交状态
	SUBMITTED("Submitted"),

	// 已批准状态
	APPROVED("Approved"),

	// 已拒绝状态
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