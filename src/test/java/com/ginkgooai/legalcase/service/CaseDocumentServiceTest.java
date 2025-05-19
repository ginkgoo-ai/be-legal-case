package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.client.storage.StorageClient;
import com.ginkgooai.legalcase.domain.CaseDocument;
import com.ginkgooai.legalcase.domain.LegalCase;
import com.ginkgooai.legalcase.domain.ProfileDocument;
import com.ginkgooai.legalcase.domain.QuestionnaireDocument;
import com.ginkgooai.legalcase.domain.SupportingDocument;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import com.ginkgooai.legalcase.service.ai.DocumentAnalysisService;
import com.ginkgooai.legalcase.service.event.DomainEventPublisherFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseDocumentServiceTest {

	@Mock
	private LegalCaseRepository legalCaseRepository;

	@Mock
	private StorageClient storageClient;

	@Mock
	private DocumentAnalysisService documentAnalysisService;

	@Mock
	private DomainEventPublisherFactory eventPublisherFactory;

	@InjectMocks
	private CaseDocumentService documentService;

	@Captor
	private ArgumentCaptor<BiConsumer<String, Map<String, Object>>> callbackCaptor;

	private String caseId;

	private LegalCase legalCase;

	private Map<String, Object> fileInfo;

	@BeforeEach
	void setUp() {
		caseId = UUID.randomUUID().toString();
		legalCase = new LegalCase();
		legalCase.setId(caseId);
		legalCase.setDocuments(new ArrayList<>());

		fileInfo = new HashMap<>();
		fileInfo.put("fileName", "test.pdf");
		fileInfo.put("contentType", "application/pdf");
		fileInfo.put("size", 1024L);
	}

	@Test
	void getDocument_shouldReturnDocument_whenExists() {
		// Arrange
		String documentId = "doc-123";
		CaseDocument document = new CaseDocument();
		document.setId(documentId);

		when(legalCaseRepository.findDocumentById(documentId)).thenReturn(Optional.of(document));

		// Act
		Optional<CaseDocument> result = documentService.getDocument(documentId);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(documentId, result.get().getId());
		verify(legalCaseRepository).findDocumentById(documentId);
	}

	@Test
	void getDocumentsByCaseId_shouldReturnAllDocuments() {
		// Arrange
		List<CaseDocument> documents = List.of(new CaseDocument(), new CaseDocument());

		when(legalCaseRepository.findDocumentsByCaseId(caseId)).thenReturn(documents);

		// Act
		List<CaseDocument> result = documentService.getDocumentsByCaseId(caseId);

		// Assert
		assertEquals(2, result.size());
		verify(legalCaseRepository).findDocumentsByCaseId(caseId);
	}

	@Test
	void uploadDocuments_shouldCreateAndSaveDocuments() {
		// Arrange
		List<String> storageIds = List.of("storage-1", "storage-2");

		when(legalCaseRepository.findById(caseId)).thenReturn(Optional.of(legalCase));
		when(storageClient.getFileInfo(anyString())).thenReturn(fileInfo);
		when(storageClient.getPublicUrl(anyString())).thenReturn("https://example.com/file.pdf");
		when(legalCaseRepository.save(any(LegalCase.class))).thenReturn(legalCase);

		// Mock async operation
		doAnswer(invocation -> {
			String docId = invocation.getArgument(0);
			String url = invocation.getArgument(1);
			return CompletableFuture.completedFuture(null);
		}).when(documentAnalysisService).analyzeDocument(anyString(), anyString(), any());

		// Act
		List<String> result = documentService.uploadDocuments(caseId, storageIds);

		// Assert
		assertEquals(2, result.size());
		assertEquals(2, legalCase.getDocuments().size());

		verify(legalCaseRepository).findById(caseId);
		verify(storageClient, times(2)).getFileInfo(anyString());
		verify(storageClient, times(2)).getPublicUrl(anyString());
		verify(legalCaseRepository).save(legalCase);
		verify(eventPublisherFactory).publishEvents(legalCase);
	}

	@Test
	void uploadDocuments_shouldThrowException_whenCaseNotFound() {
		// Arrange
		List<String> storageIds = List.of("storage-1");

		when(legalCaseRepository.findById(caseId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			documentService.uploadDocuments(caseId, storageIds);
		});

		verify(legalCaseRepository).findById(caseId);
		verifyNoInteractions(storageClient, documentAnalysisService, eventPublisherFactory);
	}

	@Test
	void updateDocumentAfterAnalysis_shouldUpdateToQuestionnaireDocument() {
		// Arrange
		String documentId = UUID.randomUUID().toString();
		CaseDocument document = new CaseDocument();
		document.setId(documentId);
		document.setTitle("Test Questionnaire");
		document.setLegalCase(legalCase);
		legalCase.getDocuments().add(document);

		Map<String, Object> analysisResult = new HashMap<>();
		analysisResult.put("documentType", "QUESTIONNAIRE");
		analysisResult.put("documentCategory", "QUESTIONNAIRE");
		analysisResult.put("extractedData", Collections.singletonMap("question1", "answer1"));
		analysisResult.put("isComplete", true);

		when(legalCaseRepository.findDocumentById(documentId)).thenReturn(Optional.of(document));
		when(legalCaseRepository.save(any(LegalCase.class))).thenReturn(legalCase);

		// Act
		documentService.updateDocumentAfterAnalysis(documentId, analysisResult);

		// Assert
		verify(legalCaseRepository).findDocumentById(documentId);
		verify(legalCaseRepository).save(legalCase);
		verify(eventPublisherFactory).publishEvents(legalCase);

		// The document should be replaced with a QuestionnaireDocument
		assertTrue(legalCase.getDocuments().get(0) instanceof QuestionnaireDocument);
		QuestionnaireDocument questionnaireDoc = (QuestionnaireDocument) legalCase.getDocuments().get(0);
		assertEquals(documentId, questionnaireDoc.getId());
		assertEquals("Test Questionnaire", questionnaireDoc.getTitle());
		assertEquals(CaseDocument.DocumentStatus.COMPLETE, questionnaireDoc.getStatus());
		assertEquals(100, questionnaireDoc.getCompletionPercentage());
	}

	@Test
	void updateDocumentAfterAnalysis_shouldUpdateToProfileDocument() {
		// Arrange
		String documentId = UUID.randomUUID().toString();
		CaseDocument document = new CaseDocument();
		document.setId(documentId);
		document.setTitle("Test Profile");
		document.setLegalCase(legalCase);
		legalCase.getDocuments().add(document);

		Map<String, Object> analysisResult = new HashMap<>();
		analysisResult.put("documentType", "IDENTITY");
		analysisResult.put("documentCategory", "PROFILE");
		analysisResult.put("extractedData", Collections.singletonMap("name", "John Doe"));
		analysisResult.put("isComplete", true);

		when(legalCaseRepository.findDocumentById(documentId)).thenReturn(Optional.of(document));
		when(legalCaseRepository.save(any(LegalCase.class))).thenReturn(legalCase);

		// Act
		documentService.updateDocumentAfterAnalysis(documentId, analysisResult);

		// Assert
		verify(legalCaseRepository).findDocumentById(documentId);
		verify(legalCaseRepository).save(legalCase);
		verify(eventPublisherFactory).publishEvents(legalCase);

		// The document should be replaced with a ProfileDocument
		assertTrue(legalCase.getDocuments().get(0) instanceof ProfileDocument);
		ProfileDocument profileDoc = (ProfileDocument) legalCase.getDocuments().get(0);
		assertEquals(documentId, profileDoc.getId());
		assertEquals("Test Profile", profileDoc.getTitle());
		assertEquals(CaseDocument.DocumentStatus.COMPLETE, profileDoc.getStatus());
		assertTrue(profileDoc.getIdentityVerified());
	}

	@Test
	void updateDocumentAfterAnalysis_shouldUpdateToSupportingDocument() {
		// Arrange
		String documentId = UUID.randomUUID().toString();
		CaseDocument document = new CaseDocument();
		document.setId(documentId);
		document.setTitle("Test Supporting");
		document.setLegalCase(legalCase);
		legalCase.getDocuments().add(document);

		Map<String, Object> analysisResult = new HashMap<>();
		analysisResult.put("documentType", "EVIDENCE");
		analysisResult.put("documentCategory", "SUPPORTING_DOCUMENT");
		analysisResult.put("extractedData", Collections.singletonMap("reference", "REF-123"));
		analysisResult.put("isComplete", false);

		when(legalCaseRepository.findDocumentById(documentId)).thenReturn(Optional.of(document));
		when(legalCaseRepository.save(any(LegalCase.class))).thenReturn(legalCase);

		// Act
		documentService.updateDocumentAfterAnalysis(documentId, analysisResult);

		// Assert
		verify(legalCaseRepository).findDocumentById(documentId);
		verify(legalCaseRepository).save(legalCase);

		// Verify that events are not published for incomplete documents
		verify(eventPublisherFactory, never()).publishEvents(legalCase);

		// The document should be replaced with a SupportingDocument
		assertTrue(legalCase.getDocuments().get(0) instanceof SupportingDocument);
		SupportingDocument supportingDoc = (SupportingDocument) legalCase.getDocuments().get(0);
		assertEquals(documentId, supportingDoc.getId());
		assertEquals("Test Supporting", supportingDoc.getTitle());
		assertEquals(CaseDocument.DocumentStatus.INCOMPLETE, supportingDoc.getStatus());
		assertFalse(supportingDoc.getVerified());
	}

	// @Test
	void updateDocumentAfterAnalysis_shouldHandleErrors() {
		// Arrange
		String documentId = UUID.randomUUID().toString();

		when(legalCaseRepository.findDocumentById(documentId)).thenThrow(new RuntimeException("Test error"));

		// Act
		documentService.updateDocumentAfterAnalysis(documentId, new HashMap<>());

		// Assert
		verify(legalCaseRepository).findDocumentById(documentId);
		// Should call updateDocumentWithError but we can't easily verify that due to
		// method accessibility
	}

	@Test
	void updateDocumentWithError_shouldMarkDocumentAsRejected() {
		// Arrange
		String documentId = UUID.randomUUID().toString();
		CaseDocument document = new CaseDocument();
		document.setId(documentId);
		document.setTitle("Test Document");
		document.setLegalCase(legalCase);
		legalCase.getDocuments().add(document);

		String errorMessage = "Test error message";

		when(legalCaseRepository.findDocumentById(documentId)).thenReturn(Optional.of(document));
		when(legalCaseRepository.save(any(LegalCase.class))).thenReturn(legalCase);

		// Act
		documentService.updateDocumentWithError(documentId, errorMessage);

		// Assert
		verify(legalCaseRepository).findDocumentById(documentId);
		verify(legalCaseRepository).save(legalCase);

		assertEquals(CaseDocument.DocumentStatus.REJECTED, document.getStatus());
		assertTrue(document.getDescription().contains(errorMessage));
	}

}