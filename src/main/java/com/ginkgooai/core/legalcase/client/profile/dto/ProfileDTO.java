package com.ginkgooai.core.legalcase.client.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

	private String id;

	private String userId;

	private String username;

	private String firstName;

	private String lastName;

	private String email;

	private String phone;

	private String address;

	private String city;

	private String state;

	private String zipCode;

	private String country;

	private String profileType;

	private String profileStatus;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime updatedAt;

}