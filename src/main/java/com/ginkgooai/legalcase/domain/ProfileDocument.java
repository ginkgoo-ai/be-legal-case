package com.ginkgooai.legalcase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Profile document type
 */
@Entity
@DiscriminatorValue("PROFILE")
@Getter
@Setter
@NoArgsConstructor
public class ProfileDocument extends CaseDocument {

	@Column(name = "profile_type")
	private String profileType;

	@Column(name = "identity_verified")
	private Boolean identityVerified;

	@Column(name = "verification_method")
	private String verificationMethod;

	/**
	 * Check if the profile information is complete
	 */
	@Override
	public boolean isComplete() {
		return getStatus() == DocumentStatus.COMPLETE || (Boolean.TRUE.equals(identityVerified));
	}

}