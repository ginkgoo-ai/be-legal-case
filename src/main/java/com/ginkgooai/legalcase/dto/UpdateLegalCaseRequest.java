package com.ginkgooai.legalcase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ginkgooai.legalcase.domain.CaseStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLegalCaseRequest {

	@Size(max = 255, message = "标题长度不能超过255个字符")
	private String title;

	@Size(max = 2000, message = "描述长度不能超过2000个字符")
	private String description;

	private CaseStatus status;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime startDate;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime endDate;

}