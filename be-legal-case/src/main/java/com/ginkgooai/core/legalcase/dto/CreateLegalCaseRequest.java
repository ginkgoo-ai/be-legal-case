package com.ginkgooai.core.legalcase.dto;

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

	@NotBlank(message = "标题不能为空")
	@Size(max = 255, message = "标题长度不能超过255个字符")
	private String title;

	@Size(max = 2000, message = "描述长度不能超过2000个字符")
	private String description;

	@NotNull(message = "客户ID不能为空")
	private UUID clientId;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime startDate;

	private UUID questionnaireResponseId;

}