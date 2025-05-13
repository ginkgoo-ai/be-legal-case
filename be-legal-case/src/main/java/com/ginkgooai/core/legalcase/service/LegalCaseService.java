package com.ginkgooai.core.legalcase.service;

import com.ginkgooai.core.legalcase.domain.LegalCase;
import com.ginkgooai.core.legalcase.dto.CreateLegalCaseRequest;
import com.ginkgooai.core.legalcase.dto.LegalCaseResponse;
import com.ginkgooai.core.legalcase.dto.UpdateLegalCaseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LegalCaseService {

	/**
	 * 创建一个新的法律案件
	 * @param request 创建案件的请求数据
	 * @param userId 创建用户的ID
	 * @return 创建的案件响应
	 */
	LegalCaseResponse createLegalCase(CreateLegalCaseRequest request, UUID userId);

	/**
	 * 获取案件详情
	 * @param caseId 案件ID
	 * @return 案件响应对象
	 */
	LegalCaseResponse getLegalCase(UUID caseId);

	/**
	 * 更新案件信息
	 * @param caseId 案件ID
	 * @param request 更新请求
	 * @return 更新后的案件响应
	 */
	LegalCaseResponse updateLegalCase(UUID caseId, UpdateLegalCaseRequest request);

	/**
	 * 删除案件
	 * @param caseId 案件ID
	 */
	void deleteLegalCase(UUID caseId);

	/**
	 * 获取用户的所有案件
	 * @param profileId 用户档案ID
	 * @param pageable 分页参数
	 * @return 案件分页列表
	 */
	Page<LegalCaseResponse> getLegalCasesByProfileId(UUID profileId, Pageable pageable);

	/**
	 * 获取客户相关的所有案件
	 * @param clientId 客户ID
	 * @param pageable 分页参数
	 * @return 案件分页列表
	 */
	Page<LegalCaseResponse> getLegalCasesByClientId(UUID clientId, Pageable pageable);

	/**
	 * 搜索案件
	 * @param searchTerm 搜索关键词
	 * @param pageable 分页参数
	 * @return 案件分页列表
	 */
	Page<LegalCaseResponse> searchLegalCases(String searchTerm, Pageable pageable);

	/**
	 * 将LegalCase实体转换为响应对象
	 * @param legalCase 案件实体
	 * @return 案件响应对象
	 */
	LegalCaseResponse convertToResponse(LegalCase legalCase);

}