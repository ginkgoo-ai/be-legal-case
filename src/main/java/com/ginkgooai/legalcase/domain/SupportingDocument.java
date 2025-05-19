package com.ginkgooai.legalcase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Supporting document type
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
	 * Check if the document is complete
	 * @return whether the document is complete
	 */
	@Override
	public boolean isComplete() {
		// Document is complete if basic information is provided
		if (getTitle() == null || getTitle().isEmpty()) {
			return false;
		}

		// If verification is required, document must be verified to be complete
		if (verificationRequired != null && verificationRequired && (verified == null || !verified)) {
			return false;
		}

		return !isExpired();
	}

	/**
	 * Check if the document is expired
	 * @return whether the document is expired
	 */
	@Transient
	public boolean isExpired() {
		if (expiryDate == null) {
			return false;
		}

		return expiryDate.isBefore(LocalDateTime.now());
	}

	/**
	 * Check if the document is required
	 * @return whether document is required
	 */
	@Transient
	public boolean isRequired() {
		return verificationRequired != null && verificationRequired;
	}
}