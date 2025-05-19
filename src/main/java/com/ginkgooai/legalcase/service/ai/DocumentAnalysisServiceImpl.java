package com.ginkgooai.legalcase.service.ai;

import com.ginkgooai.legalcase.client.ai.DocumentAIClient;
import com.ginkgooai.legalcase.client.ai.dto.DocumentParseRequest;
import com.ginkgooai.legalcase.client.ai.dto.DocumentParseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Implementation of DocumentAnalysisService using AI service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentAnalysisServiceImpl implements DocumentAnalysisService {

	private final DocumentAIClient documentAIClient;

	@Override
	public void analyzeDocument(String caseId, String documentId, String documentUrl,
			BiConsumer<Pair, Map<String, Object>> callback) {
		try {
			log.info("Analyzing document: {} with URL: {}", documentId, documentUrl);

			// Call AI service to analyze the document
			Map<String, Object> analysisResult = analyzeDocumentSync(documentUrl);

			// Process the result through the callback
			callback.accept(Pair.of(caseId, documentId), analysisResult);

			log.info("Document analysis completed for ID: {}", documentId);
		}
		catch (Exception e) {
			log.error("Error analyzing document: {}", documentId, e);
			// Create error result
			Map<String, Object> errorResult = new HashMap<>();
			errorResult.put("error", e.getMessage());
			errorResult.put("documentType", "OTHER");
			errorResult.put("documentCategory", "SUPPORTING_DOCUMENT");
			errorResult.put("isComplete", false);

			// Process the error through the callback
			callback.accept(Pair.of(caseId, documentId), errorResult);
		}
	}

	@Override
	public Map<String, Object> analyzeDocumentSync(String documentUrl) {
		log.info("Performing synchronous document analysis for URL: {}", documentUrl);

		try {
			// Create request with document URL
			DocumentParseRequest request = DocumentParseRequest.builder()
				.fileUrls(Collections.singletonList(documentUrl))
				.build();

			// Call mocked AI service to analyze the document
			DocumentParseResponse response = mockDocumentAIClientResponse(request);

			if (!"SUCCESS".equals(response.getStatus())) {
				log.warn("AI service returned non-success status: {}", response.getStatus());
				return createErrorResult("AI service returned status: " + response.getStatus());
			}

			// Process the response from AI service
			return processAIResponse(response);
		}
		catch (Exception e) {
			log.error("Error calling AI service for document analysis", e);
			return createErrorResult("Error calling AI service: " + e.getMessage());
		}
	}

	/**
	 * Mock document AI client response with 10 seconds delay
	 * @param request The document parse request
	 * @return Mocked document parse response
	 */
	private DocumentParseResponse mockDocumentAIClientResponse(DocumentParseRequest request) {
		log.info("Mocking document AI service call with 10 seconds delay");

		try {
			// Simulate network delay of 10 seconds
			TimeUnit.SECONDS.sleep(10);
		}
		catch (InterruptedException e) {
			log.warn("Mock delay was interrupted", e);
			Thread.currentThread().interrupt();
		}

		// Create random mock data
		Random random = new Random();

		// Create mocked response
		DocumentParseResponse response = new DocumentParseResponse();
		response.setStatus("SUCCESS");
		response.setMessage("Document parsed successfully");

		// Create mock ProfileData
		DocumentParseResponse.ProfileData profileData = new DocumentParseResponse.ProfileData();

		// Randomly select one document type to populate
		int docType = random.nextInt(5);

		if (docType == 0) {
			// Mock passport data
			Map<String, Object> passportData = new HashMap<>();
			passportData.put("fullName", "John Smith");
			passportData.put("dateOfBirth", "1985-05-15");
			passportData.put("passportNumber", "P123456789");
			passportData.put("expiryDate", "2030-01-01");
			passportData.put("nationality", "British");
			profileData.setPassportData(passportData);
		}
		else if (docType == 1) {
			// Mock utility bill data
			Map<String, Object> utilityBillData = new HashMap<>();
			utilityBillData.put("name", "Alice Johnson");
			utilityBillData.put("address", "123 Main St, London, UK");
			utilityBillData.put("billingDate", "2023-05-01");
			utilityBillData.put("amount", "£85.50");
			utilityBillData.put("provider", "London Energy");
			profileData.setUtilityBillData(utilityBillData);
		}
		else if (docType == 2) {
			// Mock P60 data
			Map<String, Object> p60Data = new HashMap<>();
			p60Data.put("employeeName", "Michael Brown");
			p60Data.put("taxYear", "2022-2023");
			p60Data.put("nationalInsuranceNumber", "AB123456C");
			p60Data.put("totalPay", "£45,000.00");
			p60Data.put("taxDeducted", "£9,000.00");
			profileData.setP60Data(p60Data);
		}
		else if (docType == 3) {
			// Mock referee info data
			Map<String, Object> refereeInfo = new HashMap<>();
			refereeInfo.put("refereeName", "Dr. Elizabeth Green");
			refereeInfo.put("relationship", "Former employer");
			refereeInfo.put("contactEmail", "e.green@example.com");
			refereeInfo.put("contactPhone", "07700 900123");
			refereeInfo.put("yearsKnown", "5");
			profileData.setRefereeInfo(refereeInfo);
		}
		else {
			// Mock parents info data
			Map<String, Object> parentsInfo = new HashMap<>();
			parentsInfo.put("fatherName", "Robert Wilson");
			parentsInfo.put("fatherDateOfBirth", "1960-03-20");
			parentsInfo.put("motherName", "Sarah Wilson");
			parentsInfo.put("motherDateOfBirth", "1962-07-15");
			parentsInfo.put("address", "45 Oak Avenue, Manchester, UK");
			profileData.setParentsInfo(parentsInfo);
		}

		response.setProfileData(profileData);

		log.info("Mocked response generated after delay");
		return response;
	}

	/**
	 * Process AI service response into analysis result
	 * @param response The AI service response
	 * @return Map containing analysis results
	 */
	private Map<String, Object> processAIResponse(DocumentParseResponse response) {
		Map<String, Object> result = new HashMap<>();

		// Extract document data from response
		DocumentParseResponse.ProfileData profileData = response.getProfileData();

		if (profileData == null) {
			return createErrorResult("No profile data returned from AI service");
		}

		// Determine document type and category based on the data extracted
		if (profileData.getPassportData() != null && !profileData.getPassportData().isEmpty()) {
			result.put("documentType", "IDENTITY");
			result.put("documentCategory", "PROFILE");
			result.put("extractedData", profileData.getPassportData());
			result.put("isComplete", true);
		}
		else if (profileData.getUtilityBillData() != null && !profileData.getUtilityBillData().isEmpty()) {
			result.put("documentType", "ADDRESS_PROOF");
			result.put("documentCategory", "PROFILE");
			result.put("extractedData", profileData.getUtilityBillData());
			result.put("isComplete", true);
		}
		else if (profileData.getP60Data() != null && !profileData.getP60Data().isEmpty()) {
			result.put("documentType", "FINANCIAL");
			result.put("documentCategory", "SUPPORTING_DOCUMENT");
			result.put("extractedData", profileData.getP60Data());
			result.put("isComplete", true);
		}
		else if (profileData.getRefereeInfo() != null && !profileData.getRefereeInfo().isEmpty()) {
			result.put("documentType", "QUESTIONNAIRE");
			result.put("documentCategory", "QUESTIONNAIRE");
			result.put("extractedData", profileData.getRefereeInfo());
			result.put("isComplete", false); // Questionnaires typically need manual
												// review
		}
		else if (profileData.getParentsInfo() != null && !profileData.getParentsInfo().isEmpty()) {
			result.put("documentType", "APPLICANT");
			result.put("documentCategory", "PROFILE");
			result.put("extractedData", profileData.getRefereeInfo());
			result.put("isComplete", false); // Questionnaires typically need manual
												// review
		}
		else {
			// Default handling for unrecognized document types
			result.put("documentType", "OTHER");
			result.put("documentCategory", "SUPPORTING_DOCUMENT");
			result.put("extractedData", new HashMap<>());
			result.put("isComplete", false);
		}

		return result;
	}

	/**
	 * Create an error result
	 * @param errorMessage The error message
	 * @return Map containing error information
	 */
	private Map<String, Object> createErrorResult(String errorMessage) {
		Map<String, Object> result = new HashMap<>();
		result.put("error", errorMessage);
		result.put("documentType", "OTHER");
		result.put("documentCategory", "SUPPORTING_DOCUMENT");
		result.put("extractedData", new HashMap<>());
		result.put("isComplete", false);
		return result;
	}

}