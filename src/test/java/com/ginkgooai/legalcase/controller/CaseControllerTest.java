package com.ginkgooai.legalcase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ginkgooai.legalcase.domain.CaseStatus;
import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.dto.CaseCreationRequest;
import com.ginkgooai.legalcase.dto.LegalCaseResponse;
import com.ginkgooai.legalcase.dto.UpdateLegalCaseRequest;
import com.ginkgooai.legalcase.service.CaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CaseControllerTest {

	private static final String BASE_URL = "/cases";

	@Mock
	private CaseService caseService;

	@InjectMocks
	private CaseController caseController;

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules(); // 支持 Java 8 日期时间类型

		mockMvc = MockMvcBuilders.standaloneSetup(caseController)
			.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
			.build();
	}

	@Test
	void createEmptyCase_shouldReturnCreatedCase() throws Exception {
		// Arrange
		String caseId = UUID.randomUUID().toString();
		String title = "Test Case";
		String description = "Test Description";
		String profileId = "profile-1";
		String clientId = "client-1";

		CaseCreationRequest request = CaseCreationRequest.builder()
			.title(title)
			.description(description)
			.profileId(profileId)
			.clientId(clientId)
			.build();

		LegalCase legalCase = createTestLegalCase(caseId, title, description, profileId, clientId);

		LegalCaseResponse response = LegalCaseResponse.builder()
			.id(caseId)
			.title(title)
			.description(description)
			.caseNumber("CASE-12345678")
			.profileId(profileId)
			.clientId(clientId)
			.status(CaseStatus.CREATED)
			.build();

		when(caseService.createEmptyCase(title, description, profileId, clientId)).thenReturn(legalCase);
		when(caseService.convertToResponse(legalCase)).thenReturn(response);

		// Act & Assert
		mockMvc
			.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(caseId)))
			.andExpect(jsonPath("$.title", is(title)))
			.andExpect(jsonPath("$.description", is(description)))
			.andExpect(jsonPath("$.profileId", is(profileId)))
			.andExpect(jsonPath("$.clientId", is(clientId)))
			.andExpect(jsonPath("$.status", is(CaseStatus.CREATED.name())));

		verify(caseService).createEmptyCase(title, description, profileId, clientId);
		verify(caseService).convertToResponse(legalCase);
	}

	@Test
	void getLegalCase_shouldReturnCase() throws Exception {
		// Arrange
		String caseId = UUID.randomUUID().toString();

		LegalCaseResponse response = LegalCaseResponse.builder()
			.id(caseId)
			.title("Test Case")
			.description("Test Description")
			.caseNumber("CASE-12345678")
			.status(CaseStatus.DOCUMENTATION_IN_PROGRESS)
			.build();

		when(caseService.getLegalCase(caseId)).thenReturn(response);

		// Act & Assert
		mockMvc.perform(get(BASE_URL + "/{id}", caseId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(caseId)))
			.andExpect(jsonPath("$.title", is("Test Case")))
			.andExpect(jsonPath("$.status", is(CaseStatus.DOCUMENTATION_IN_PROGRESS.name())));

		verify(caseService).getLegalCase(caseId);
	}

	@Test
	void getLegalCase_notFound_shouldReturn404() throws Exception {
		// Arrange
		String caseId = UUID.randomUUID().toString();

		when(caseService.getLegalCase(caseId)).thenThrow(new ResourceNotFoundException("案件不存在: " + caseId));

		// Act & Assert
		mockMvc.perform(get(BASE_URL + "/{id}", caseId)).andExpect(status().isNotFound());

		verify(caseService).getLegalCase(caseId);
	}

	@Test
	void updateLegalCase_shouldReturnUpdatedCase() throws Exception {
		// Arrange
		String caseId = UUID.randomUUID().toString();

		UpdateLegalCaseRequest request = UpdateLegalCaseRequest.builder()
			.title("Updated Title")
			.description("Updated Description")
			.status(CaseStatus.ON_HOLD)
			.build();

		LegalCaseResponse response = LegalCaseResponse.builder()
			.id(caseId)
			.title("Updated Title")
			.description("Updated Description")
			.status(CaseStatus.ON_HOLD)
			.build();

		when(caseService.updateLegalCase(eq(caseId), any(UpdateLegalCaseRequest.class))).thenReturn(response);

		// Act & Assert
		mockMvc
			.perform(put(BASE_URL + "/{id}", caseId).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(caseId)))
			.andExpect(jsonPath("$.title", is("Updated Title")))
			.andExpect(jsonPath("$.description", is("Updated Description")))
			.andExpect(jsonPath("$.status", is(CaseStatus.ON_HOLD.name())));

		verify(caseService).updateLegalCase(eq(caseId), any(UpdateLegalCaseRequest.class));
	}

	@Test
	void deleteLegalCase_shouldReturn204() throws Exception {
		// Arrange
		String caseId = UUID.randomUUID().toString();

		// Act & Assert
		mockMvc.perform(delete(BASE_URL + "/{id}", caseId)).andExpect(status().isNoContent());

		verify(caseService).deleteLegalCase(caseId);
	}

	@Test
	void deleteLegalCase_notFound_shouldReturn404() throws Exception {
		// Arrange
		String caseId = UUID.randomUUID().toString();

		doThrow(new ResourceNotFoundException("案件不存在: " + caseId)).when(caseService).deleteLegalCase(caseId);

		// Act & Assert
		mockMvc.perform(delete(BASE_URL + "/{id}", caseId)).andExpect(status().isNotFound());

		verify(caseService).deleteLegalCase(caseId);
	}

	// @Test
	void searchLegalCases_shouldReturnMatchingCases() throws Exception {
		// Arrange
		String searchTerm = "test";
		Pageable pageable = PageRequest.of(0, 10);

		LegalCaseResponse case1 = LegalCaseResponse.builder()
			.id(UUID.randomUUID().toString())
			.title("Test Case 1")
			.build();

		LegalCaseResponse case2 = LegalCaseResponse.builder()
			.id(UUID.randomUUID().toString())
			.title("Test Case 2")
			.build();

		Page<LegalCaseResponse> page = new PageImpl<>(List.of(case1, case2));

		when(caseService.searchLegalCases(eq(searchTerm), any(Pageable.class))).thenReturn(page);

		// Act & Assert
		mockMvc.perform(get(BASE_URL + "/search").param("q", searchTerm))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", hasSize(2)))
			.andExpect(jsonPath("$.content[0].title", is("Test Case 1")))
			.andExpect(jsonPath("$.content[1].title", is("Test Case 2")));

		verify(caseService).searchLegalCases(eq(searchTerm), any(Pageable.class));
	}

	private LegalCase createTestLegalCase(String id, String title, String description, String profileId,
			String clientId) {
		LegalCase legalCase = LegalCase.builder()
			.id(id)
			.title(title)
			.description(description)
			.caseNumber("CASE-12345678")
			.profileId(profileId)
			.clientId(clientId)
			.status(CaseStatus.CREATED)
			.build();

		// 手动设置创建和更新时间
		legalCase.setCreatedAt(LocalDateTime.now());
		legalCase.setUpdatedAt(LocalDateTime.now());

		return legalCase;
	}

}