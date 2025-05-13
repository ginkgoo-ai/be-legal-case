package com.ginkgooai.core.legalcase.service;

import com.ginkgooai.core.legalcase.client.gatekeeper.GatekeeperClient;
import com.ginkgooai.core.legalcase.client.gatekeeper.dto.QuestionnaireResponseDTO;
import com.ginkgooai.core.legalcase.client.profile.ProfileClient;
import com.ginkgooai.core.legalcase.client.profile.dto.ProfileDTO;
import com.ginkgooai.core.legalcase.domain.LegalCase;
import com.ginkgooai.core.legalcase.dto.CreateLegalCaseRequest;
import com.ginkgooai.core.legalcase.dto.LegalCaseResponse;
import com.ginkgooai.core.legalcase.dto.UpdateLegalCaseRequest;
import com.ginkgooai.core.legalcase.exception.ResourceNotFoundException;
import com.ginkgooai.core.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.core.legalcase.util.CaseNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegalCaseServiceImpl implements LegalCaseService {

	private final LegalCaseRepository legalCaseRepository;

	private final ProfileClient profileClient;

	private final GatekeeperClient gatekeeperClient;

	private final CaseNumberGenerator caseNumberGenerator;

	@Override
	@Transactional
	public LegalCaseResponse createLegalCase(CreateLegalCaseRequest request, UUID userId) {
		log.info("创建法律案件：title={}, userId={}", request.getTitle(), userId);

		// 获取用户档案信息
		ProfileDTO profile = profileClient.getProfileByUserId(userId);

		// 创建案件基本信息
		LegalCase legalCase = LegalCase.builder()
			.title(request.getTitle())
			.description(request.getDescription())
			.caseNumber(caseNumberGenerator.generate())
			.profileId(profile.getId())
			.clientId(request.getClientId())
			.status(CaseStatus.OPEN)
			.startDate(request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now())
			.build();

		// 如果提供了问卷响应ID，则获取问卷信息用来丰富案件描述
		if (request.getQuestionnaireResponseId() != null) {
			try {
				QuestionnaireResponseDTO questionnaireResponse = gatekeeperClient
					.getQuestionnaireResponseById(request.getQuestionnaireResponseId());

				if (questionnaireResponse != null) {
					// 将问卷信息添加到描述中或处理问卷数据
					updateCaseWithQuestionnaireData(legalCase, questionnaireResponse);
				}
			}
			catch (Exception e) {
				log.error("获取问卷响应数据失败", e);
				// 继续处理，不影响案件创建
			}
		}

		// 保存案件
		LegalCase savedCase = legalCaseRepository.save(legalCase);
		return convertToResponse(savedCase);
	}

	@Override
	@Transactional(readOnly = true)
	public LegalCaseResponse getLegalCase(UUID caseId) {
		LegalCase legalCase = findLegalCaseById(caseId);
		return convertToResponse(legalCase);
	}

	@Override
	@Transactional
	public LegalCaseResponse updateLegalCase(UUID caseId, UpdateLegalCaseRequest request) {
		LegalCase legalCase = findLegalCaseById(caseId);

		// 更新案件信息
		if (request.getTitle() != null) {
			legalCase.setTitle(request.getTitle());
		}

		if (request.getDescription() != null) {
			legalCase.setDescription(request.getDescription());
		}

		if (request.getStatus() != null) {
			legalCase.setStatus(request.getStatus());

			// 如果状态变为已关闭，自动设置结束日期
			if (request.getStatus() == CaseStatus.CLOSED && legalCase.getEndDate() == null) {
				legalCase.setEndDate(LocalDateTime.now());
			}
		}

		if (request.getStartDate() != null) {
			legalCase.setStartDate(request.getStartDate());
		}

		if (request.getEndDate() != null) {
			legalCase.setEndDate(request.getEndDate());
		}

		LegalCase updatedCase = legalCaseRepository.save(legalCase);
		return convertToResponse(updatedCase);
	}

	@Override
	@Transactional
	public void deleteLegalCase(UUID caseId) {
		LegalCase legalCase = findLegalCaseById(caseId);
		legalCaseRepository.delete(legalCase);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<LegalCaseResponse> getLegalCasesByProfileId(UUID profileId, Pageable pageable) {
		Page<LegalCase> cases = legalCaseRepository.findByProfileId(profileId, pageable);
		return cases.map(this::convertToResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<LegalCaseResponse> getLegalCasesByClientId(UUID clientId, Pageable pageable) {
		Page<LegalCase> cases = legalCaseRepository.findByClientId(clientId, pageable);
		return cases.map(this::convertToResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<LegalCaseResponse> searchLegalCases(String searchTerm, Pageable pageable) {
		Page<LegalCase> cases = legalCaseRepository.search(searchTerm, pageable);
		return cases.map(this::convertToResponse);
	}

	@Override
	public LegalCaseResponse convertToResponse(LegalCase legalCase) {
		LegalCaseResponse response = LegalCaseResponse.builder()
			.id(legalCase.getId())
			.title(legalCase.getTitle())
			.description(legalCase.getDescription())
			.caseNumber(legalCase.getCaseNumber())
			.profileId(legalCase.getProfileId())
			.clientId(legalCase.getClientId())
			.status(legalCase.getStatus())
			.startDate(legalCase.getStartDate())
			.endDate(legalCase.getEndDate())
			.createdAt(legalCase.getCreatedAt())
			.updatedAt(legalCase.getUpdatedAt())
			.documentsCount(legalCase.getDocuments().size())
			.notesCount(legalCase.getNotes().size())
			.eventsCount(legalCase.getEvents().size())
			.build();

		// 尝试获取客户名称
		try {
			if (legalCase.getClientId() != null) {
				ProfileDTO clientProfile = profileClient.getProfileById(legalCase.getClientId());
				if (clientProfile != null) {
					response.setClientName(clientProfile.getFirstName() + " " + clientProfile.getLastName());
				}
			}
		}
		catch (Exception e) {
			log.error("获取客户信息失败", e);
		}

		// 尝试获取律师名称
		try {
			ProfileDTO lawyerProfile = profileClient.getProfileById(legalCase.getProfileId());
			if (lawyerProfile != null) {
				response.setProfileName(lawyerProfile.getFirstName() + " " + lawyerProfile.getLastName());
			}
		}
		catch (Exception e) {
			log.error("获取律师信息失败", e);
		}

		return response;
	}

	// 辅助方法

	private LegalCase findLegalCaseById(UUID caseId) {
		return legalCaseRepository.findById(caseId)
			.orElseThrow(() -> new ResourceNotFoundException("案件不存在: " + caseId));
	}

	private void updateCaseWithQuestionnaireData(LegalCase legalCase, QuestionnaireResponseDTO questionnaireResponse) {
		// 获取问卷回答数据
		Map<String, Object> responses = questionnaireResponse.getResponses();

		// 根据问卷类型进行不同处理
		// 这里仅作为示例，实际业务可能更复杂
		if ("LEGAL_INTAKE".equals(questionnaireResponse.getQuestionnaireType())) {
			// 添加一些问卷中的关键信息到案件描述
			StringBuilder enhancedDescription = new StringBuilder();

			if (legalCase.getDescription() != null && !legalCase.getDescription().isBlank()) {
				enhancedDescription.append(legalCase.getDescription()).append("\n\n");
			}

			enhancedDescription.append("来自客户问卷的附加信息：\n");

			// 示例：从问卷提取信息
			if (responses.containsKey("legalIssueCategory")) {
				enhancedDescription.append("法律问题类别: ").append(responses.get("legalIssueCategory")).append("\n");
			}

			if (responses.containsKey("incidentDate")) {
				enhancedDescription.append("事件发生日期: ").append(responses.get("incidentDate")).append("\n");
			}

			if (responses.containsKey("caseBackground")) {
				enhancedDescription.append("案件背景: ").append(responses.get("caseBackground")).append("\n");
			}

			legalCase.setDescription(enhancedDescription.toString());
		}
	}

}