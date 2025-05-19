package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.domain.CaseDocument;
import com.ginkgooai.legalcase.domain.CaseStatus;
import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.dto.LegalCaseResponse;
import com.ginkgooai.legalcase.dto.UpdateLegalCaseRequest;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.event.DomainEventPublisherFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

	@Mock
	private LegalCaseRepository legalCaseRepository;

	@Mock
	private DomainEventPublisherFactory eventPublisherFactory;

	@InjectMocks
	private CaseService caseService;

	private String testCaseId;

	private LegalCase testLegalCase;

	@BeforeEach
	void setUp() {
		testCaseId = UUID.randomUUID().toString();
		testLegalCase = LegalCase.builder()
			.id(testCaseId)
			.title("Test Case")
			.description("Test Description")
			.caseNumber("CASE-12345678")
			.profileId("profile-1")
			.clientId("client-1")
			.status(CaseStatus.CREATED)
			.documents(new ArrayList<>())
			.events(new ArrayList<>())
			.build();
	}

	@Test
	void findDocumentById_shouldReturnDocument_whenDocumentExists() {
		// Arrange
		String documentId = UUID.randomUUID().toString();
		CaseDocument document = new CaseDocument();
		document.setId(documentId);

		when(legalCaseRepository.findDocumentById(documentId)).thenReturn(Optional.of(document));

		// Act
		Optional<CaseDocument> result = caseService.findDocumentById(documentId);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(documentId, result.get().getId());
		verify(legalCaseRepository).findDocumentById(documentId);
	}

	@Test
	void findDocumentById_shouldReturnEmpty_whenDocumentDoesNotExist() {
		// Arrange
		String documentId = UUID.randomUUID().toString();

		when(legalCaseRepository.findDocumentById(documentId)).thenReturn(Optional.empty());

		// Act
		Optional<CaseDocument> result = caseService.findDocumentById(documentId);

		// Assert
		assertTrue(result.isEmpty());
		verify(legalCaseRepository).findDocumentById(documentId);
	}

	@Test
	void createEmptyCase_shouldCreateAndSaveCase() {
		// Arrange
		String title = "New Case";
		String description = "New Description";
		String profileId = "profile-1";
		String clientId = "client-1";

		ArgumentCaptor<LegalCase> caseCaptor = ArgumentCaptor.forClass(LegalCase.class);

		when(legalCaseRepository.save(any(LegalCase.class))).thenReturn(testLegalCase);

		// Act
		LegalCase result = caseService.createEmptyCase(title, description, profileId, clientId);

		// Assert
		verify(legalCaseRepository).save(caseCaptor.capture());
		verify(eventPublisherFactory).publishEvents(testLegalCase);

		LegalCase capturedCase = caseCaptor.getValue();
		assertNotNull(capturedCase);
		assertEquals(title, capturedCase.getTitle());
		assertEquals(description, capturedCase.getDescription());
		assertEquals(profileId, capturedCase.getProfileId());
		assertEquals(clientId, capturedCase.getClientId());
		assertEquals(CaseStatus.DOCUMENTATION_IN_PROGRESS, capturedCase.getStatus()); // Status
																						// changes
																						// to
																						// DOCUMENTATION_IN_PROGRESS
																						// after
																						// createCase()
		assertNotNull(capturedCase.getCaseNumber());
		assertTrue(capturedCase.getCaseNumber().startsWith("CASE-"));

		assertEquals(testLegalCase, result);
	}

	@Test
	void selectQuestionnaireTemplates_shouldCreateQuestionnaireDocuments() {
		// Arrange
		List<String> templateIds = List.of("template-1", "template-2");

		when(legalCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testLegalCase));
		when(legalCaseRepository.save(any(LegalCase.class))).thenReturn(testLegalCase);

		// Act
		Map<String, String> result = caseService.selectQuestionnaireTemplates(testCaseId, templateIds);

		// Assert
		verify(legalCaseRepository).findById(testCaseId);
		verify(legalCaseRepository).save(testLegalCase);
		verify(eventPublisherFactory).publishEvents(testLegalCase);

		assertEquals(2, result.size());
		assertTrue(result.containsKey("template-1"));
		assertTrue(result.containsKey("template-2"));
	}

	@Test
	void selectQuestionnaireTemplates_shouldThrowException_whenCaseNotFound() {
		// Arrange
		List<String> templateIds = List.of("template-1", "template-2");

		when(legalCaseRepository.findById(testCaseId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> {
			caseService.selectQuestionnaireTemplates(testCaseId, templateIds);
		});

		verify(legalCaseRepository).findById(testCaseId);
		verifyNoMoreInteractions(legalCaseRepository);
		verifyNoInteractions(eventPublisherFactory);
	}

	@Test
	void getLegalCase_shouldReturnResponse_whenCaseExists() {
		// Arrange
		when(legalCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testLegalCase));

		// Act
		LegalCaseResponse response = caseService.getLegalCase(testCaseId);

		// Assert
		assertNotNull(response);
		assertEquals(testCaseId, response.getId());
		assertEquals(testLegalCase.getTitle(), response.getTitle());
		assertEquals(testLegalCase.getDescription(), response.getDescription());
		assertEquals(testLegalCase.getStatus(), response.getStatus());

		verify(legalCaseRepository).findById(testCaseId);
	}

	@Test
	void getLegalCase_shouldThrowException_whenCaseNotFound() {
		// Arrange
		when(legalCaseRepository.findById(testCaseId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(ResourceNotFoundException.class, () -> {
			caseService.getLegalCase(testCaseId);
		});

		verify(legalCaseRepository).findById(testCaseId);
	}

	@Test
	void updateLegalCase_shouldUpdateAndReturnCase() {
		// Arrange
		UpdateLegalCaseRequest request = UpdateLegalCaseRequest.builder()
			.title("Updated Title")
			.description("Updated Description")
			.status(CaseStatus.ON_HOLD)
			.build();

		when(legalCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testLegalCase));
		when(legalCaseRepository.save(any(LegalCase.class))).thenReturn(testLegalCase);

		// Act
		LegalCaseResponse response = caseService.updateLegalCase(testCaseId, request);

		// Assert
		assertNotNull(response);
		assertEquals(testCaseId, response.getId());

		verify(legalCaseRepository).findById(testCaseId);
		verify(legalCaseRepository).save(testLegalCase);

		assertEquals("Updated Title", testLegalCase.getTitle());
		assertEquals("Updated Description", testLegalCase.getDescription());
		assertEquals(CaseStatus.ON_HOLD, testLegalCase.getStatus());
	}

	@Test
	void updateLegalCase_shouldSetEndDate_whenStatusIsDenied() {
		// Arrange
		UpdateLegalCaseRequest request = UpdateLegalCaseRequest.builder().status(CaseStatus.DENIED).build();

		when(legalCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testLegalCase));
		when(legalCaseRepository.save(any(LegalCase.class))).thenReturn(testLegalCase);

		// Act
		caseService.updateLegalCase(testCaseId, request);

		// Assert
		assertNotNull(testLegalCase.getEndDate());
		assertEquals(CaseStatus.DENIED, testLegalCase.getStatus());

		verify(legalCaseRepository).findById(testCaseId);
		verify(legalCaseRepository).save(testLegalCase);
	}

	@Test
	void updateLegalCase_shouldThrowException_whenCaseNotFound() {
		// Arrange
		UpdateLegalCaseRequest request = UpdateLegalCaseRequest.builder().title("Updated Title").build();

		when(legalCaseRepository.findById(testCaseId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(ResourceNotFoundException.class, () -> {
			caseService.updateLegalCase(testCaseId, request);
		});

		verify(legalCaseRepository).findById(testCaseId);
		verifyNoMoreInteractions(legalCaseRepository);
	}

	@Test
	void deleteLegalCase_shouldDeleteCase() {
		// Arrange
		when(legalCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testLegalCase));

		// Act
		caseService.deleteLegalCase(testCaseId);

		// Assert
		verify(legalCaseRepository).findById(testCaseId);
		verify(legalCaseRepository).delete(testLegalCase);
	}

	@Test
	void deleteLegalCase_shouldThrowException_whenCaseNotFound() {
		// Arrange
		when(legalCaseRepository.findById(testCaseId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(ResourceNotFoundException.class, () -> {
			caseService.deleteLegalCase(testCaseId);
		});

		verify(legalCaseRepository).findById(testCaseId);
		verifyNoMoreInteractions(legalCaseRepository);
	}

	@Test
	void getLegalCasesByProfileId_shouldReturnCases() {
		// Arrange
		String profileId = "profile-1";
		Pageable pageable = PageRequest.of(0, 10);

		Page<LegalCase> cases = new PageImpl<>(List.of(testLegalCase));

		when(legalCaseRepository.findByProfileId(profileId, pageable)).thenReturn(cases);

		// Act
		Page<LegalCaseResponse> response = caseService.getLegalCasesByProfileId(profileId, pageable);

		// Assert
		assertNotNull(response);
		assertEquals(1, response.getTotalElements());
		assertEquals(testCaseId, response.getContent().get(0).getId());

		verify(legalCaseRepository).findByProfileId(profileId, pageable);
	}

	@Test
	void getLegalCasesByClientId_shouldReturnCases() {
		// Arrange
		String clientId = "client-1";
		Pageable pageable = PageRequest.of(0, 10);

		Page<LegalCase> cases = new PageImpl<>(List.of(testLegalCase));

		when(legalCaseRepository.findByClientId(clientId, pageable)).thenReturn(cases);

		// Act
		Page<LegalCaseResponse> response = caseService.getLegalCasesByClientId(clientId, pageable);

		// Assert
		assertNotNull(response);
		assertEquals(1, response.getTotalElements());
		assertEquals(testCaseId, response.getContent().get(0).getId());

		verify(legalCaseRepository).findByClientId(clientId, pageable);
	}

	@Test
	void searchLegalCases_shouldReturnMatchingCases() {
		// Arrange
		String searchTerm = "test";
		Pageable pageable = PageRequest.of(0, 10);

		Page<LegalCase> cases = new PageImpl<>(List.of(testLegalCase));

		when(legalCaseRepository.searchCases(searchTerm, pageable)).thenReturn(cases);

		// Act
		Page<LegalCaseResponse> response = caseService.searchLegalCases(searchTerm, pageable);

		// Assert
		assertNotNull(response);
		assertEquals(1, response.getTotalElements());
		assertEquals(testCaseId, response.getContent().get(0).getId());

		verify(legalCaseRepository).searchCases(searchTerm, pageable);
	}

	@Test
	void convertToResponse_shouldConvertCorrectly() {
		// Arrange & Act
		LegalCaseResponse response = caseService.convertToResponse(testLegalCase);

		// Assert
		assertNotNull(response);
		assertEquals(testCaseId, response.getId());
		assertEquals(testLegalCase.getTitle(), response.getTitle());
		assertEquals(testLegalCase.getDescription(), response.getDescription());
		assertEquals(testLegalCase.getCaseNumber(), response.getCaseNumber());
		assertEquals(testLegalCase.getProfileId(), response.getProfileId());
		assertEquals(testLegalCase.getClientId(), response.getClientId());
		assertEquals(testLegalCase.getStatus(), response.getStatus());
		assertEquals(0, response.getDocumentsCount());
		assertEquals(0, response.getEventsCount());
	}

}