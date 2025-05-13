package com.ginkgooai.core.legalcase.controller;

import com.ginkgooai.core.legalcase.dto.CreateLegalCaseRequest;
import com.ginkgooai.core.legalcase.dto.LegalCaseResponse;
import com.ginkgooai.core.legalcase.dto.UpdateLegalCaseRequest;
import com.ginkgooai.core.legalcase.service.LegalCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class LegalCaseController {

	private final LegalCaseService legalCaseService;

	/**
	 * 创建新的法律案件
	 */
	@PostMapping
	public ResponseEntity<LegalCaseResponse> createLegalCase(@Valid @RequestBody CreateLegalCaseRequest request,
			@AuthenticationPrincipal Jwt jwt) {

		UUID userId = UUID.fromString(jwt.getSubject());
		LegalCaseResponse response = legalCaseService.createLegalCase(request, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 获取案件详情
	 */
	@GetMapping("/{id}")
	public ResponseEntity<LegalCaseResponse> getLegalCase(@PathVariable("id") UUID id) {
		LegalCaseResponse response = legalCaseService.getLegalCase(id);
		return ResponseEntity.ok(response);
	}

	/**
	 * 更新案件信息
	 */
	@PutMapping("/{id}")
	public ResponseEntity<LegalCaseResponse> updateLegalCase(@PathVariable("id") UUID id,
			@Valid @RequestBody UpdateLegalCaseRequest request) {

		LegalCaseResponse response = legalCaseService.updateLegalCase(id, request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 删除案件
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteLegalCase(@PathVariable("id") UUID id) {
		legalCaseService.deleteLegalCase(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 根据律师档案ID获取案件列表
	 */
	@GetMapping("/profile/{profileId}")
	public ResponseEntity<Page<LegalCaseResponse>> getLegalCasesByProfileId(@PathVariable("profileId") UUID profileId,
			@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		Page<LegalCaseResponse> response = legalCaseService.getLegalCasesByProfileId(profileId, pageable);
		return ResponseEntity.ok(response);
	}

	/**
	 * 根据客户ID获取案件列表
	 */
	@GetMapping("/client/{clientId}")
	public ResponseEntity<Page<LegalCaseResponse>> getLegalCasesByClientId(@PathVariable("clientId") UUID clientId,
			@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		Page<LegalCaseResponse> response = legalCaseService.getLegalCasesByClientId(clientId, pageable);
		return ResponseEntity.ok(response);
	}

	/**
	 * 搜索案件
	 */
	@GetMapping("/search")
	public ResponseEntity<Page<LegalCaseResponse>> searchLegalCases(@RequestParam("q") String searchTerm,
			@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		Page<LegalCaseResponse> response = legalCaseService.searchLegalCases(searchTerm, pageable);
		return ResponseEntity.ok(response);
	}

	/**
	 * 获取当前用户的案件列表
	 */
	@GetMapping("/my")
	public ResponseEntity<Page<LegalCaseResponse>> getMyLegalCases(@AuthenticationPrincipal Jwt jwt,
			@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		UUID userId = UUID.fromString(jwt.getSubject());
		// 通过ProfileClient获取用户的profileId
		// 实际项目中应该有更好的方式处理这种关联查询
		try {
			return ResponseEntity.ok(legalCaseService.getLegalCasesByProfileId(userId, pageable));
		}
		catch (Exception e) {
			// 如果userId和profileId不一致，这里需要做适当处理
			return ResponseEntity.ok(Page.empty());
		}
	}

}