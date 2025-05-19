package com.ginkgooai.legalcase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLegalCaseRequest {

	@NotBlank(message = "Title cannot be empty")
	@Size(max = 255, message = "Title cannot exceed 255 characters")
	private String title;

	@Size(max = 2000, message = "Description cannot exceed 2000 characters")
	private String description;

	@NotNull(message = "Client ID cannot be empty")
	private String clientId;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime startDate;
}