package com.ginkgooai.core.legalcase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseDocumentRequest {

	@NotBlank(message = "文档标题不能为空")
	@Size(max = 255, message = "标题长度不能超过255个字符")
	private String title;

	@Size(max = 1000, message = "描述长度不能超过1000个字符")
	private String description;

	private String documentType;

}