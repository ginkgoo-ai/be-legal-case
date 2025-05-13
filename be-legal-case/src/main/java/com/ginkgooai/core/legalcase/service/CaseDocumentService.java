package com.ginkgooai.core.legalcase.service;

import com.ginkgooai.core.legalcase.dto.request.CreateCaseDocumentRequest;
import com.ginkgooai.core.legalcase.dto.response.CaseDocumentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface CaseDocumentService {

	/**
	 * 为案件上传文档
	 * @param caseId 案件ID
	 * @param file 文件
	 * @param request 文档请求
	 * @return 文档响应
	 */
	CaseDocumentResponse uploadDocument(UUID caseId, MultipartFile file, CreateCaseDocumentRequest request);

	/**
	 * 获取案件的所有文档
	 * @param caseId 案件ID
	 * @param pageable 分页参数
	 * @return 文档分页列表
	 */
	Page<CaseDocumentResponse> getDocumentsByCaseId(UUID caseId, Pageable pageable);

	/**
	 * 获取文档
	 * @param caseId 案件ID
	 * @param documentId 文档ID
	 * @return 文档响应
	 */
	CaseDocumentResponse getDocument(UUID caseId, UUID documentId);

	/**
	 * 更新文档
	 * @param caseId 案件ID
	 * @param documentId 文档ID
	 * @param request 更新请求
	 * @return 更新后的文档响应
	 */
	CaseDocumentResponse updateDocument(UUID caseId, UUID documentId, CreateCaseDocumentRequest request);

	/**
	 * 删除文档
	 * @param caseId 案件ID
	 * @param documentId 文档ID
	 */
	void deleteDocument(UUID caseId, UUID documentId);

}