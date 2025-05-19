package com.ginkgooai.legalcase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 支持文档类型 Supporting document type
 */
@Entity
@DiscriminatorValue("SUPPORTING_DOCUMENT")
@Getter
@Setter
@NoArgsConstructor
public class SupportingDocument extends CaseDocument {

	@Column(name = "document_reference")
	private String documentReference;

	@Column(name = "issuing_authority")
	private String issuingAuthority;

	@Column(name = "issue_date")
	private LocalDateTime issueDate;

	@Column(name = "expiry_date")
	private LocalDateTime expiryDate;

	@Column(name = "verification_required")
	private Boolean verificationRequired;

	@Column(name = "verified")
	private Boolean verified;

	/**
	 * Check if the supporting document is complete
	 */
	@Override
	public boolean isComplete() {
		// If verification is not required, the document is complete when its status is
		// COMPLETE
		if (Boolean.FALSE.equals(verificationRequired)) {
			return getStatus() == DocumentStatus.COMPLETE;
		}

		// If verification is required, the document must be verified
		return getStatus() == DocumentStatus.COMPLETE && Boolean.TRUE.equals(verified);
	}

	/**
	 * Check if the document is expired
	 */
	public boolean isExpired() {
		if (expiryDate == null) {
			return false;
		}
		return LocalDateTime.now().isAfter(expiryDate);
	}

	/**
	 * 检查文档是否为必需 Check if the document is required
	 * @return 文档是否为必需 / whether document is required
	 */
	public boolean isRequired() {
		return true;
	}

}