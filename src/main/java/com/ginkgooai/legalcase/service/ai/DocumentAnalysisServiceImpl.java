package com.ginkgooai.legalcase.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ginkgooai.legalcase.client.ai.DocumentAIClient;
import com.ginkgooai.legalcase.client.ai.dto.DocumentParseRequest;
import com.ginkgooai.legalcase.client.ai.dto.DocumentParseResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
			// DocumentParseResponse response = documentAIClient.parseDocuments(request);

			if (response == null) {
				log.warn("AI service returned non-success");
				return createErrorResult("AI service error");
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
	@SneakyThrows
	private DocumentParseResponse mockDocumentAIClientResponse(DocumentParseRequest request) {
		log.info("Mocking document AI service call with 10 seconds delay");

		try {
			// Simulate network delay of 10 seconds
			TimeUnit.SECONDS.sleep(5);
		}
		catch (InterruptedException e) {
			log.warn("Mock delay was interrupted", e);
			Thread.currentThread().interrupt();
		}

		// Create mocked response
		DocumentParseResponse response = new DocumentParseResponse();

		ObjectMapper objectMapper = new ObjectMapper();

		response = objectMapper.readValue(
				"{\"utility_bill\":{\"bank\":{\"name\":\"Santander\",\"type\":null,\"address\":{\"street\":null,\"city\":\"Bootle\",\"postcode\":\"GIR OAA\"}},\"transaction_details\":{\"reference_number\":\"9000744775033\",\"credit_account_number\":null,\"amount\":\"724.26\",\"standard_fee_payable\":null},\"payment_instructions\":{\"cheque_acceptance\":\"Cheque NOT acceptable at Post Office\",\"bank_details\":{\"bank_name\":null,\"account_name\":\"Collection Account\\nThames Water\\nUtilities Ltd\",\"sort_code\":\"57-27-53\",\"account_number\":null,\"iban\":null,\"swift_bic\":null},\"cash_payment_note\":null},\"payee\":{\"name\":null,\"address\":{\"street\":\"51 Redcliffe Square\",\"city\":\"SW10 9HG\",\"postcode\":null}},\"signature\":null,\"date\":null,\"additional_codes\":{\"code_1\":\"9826 9274 0290 0074 4775 03 0\",\"code_2\":\"257 2753\",\"code_3\":null},\"notes\":{\"do_not_write_below_line\":\"Please do not write or mark below this line and do not fold this counterfoil\",\"do_not_fold_counterfoil\":null}}}",
				DocumentParseResponse.class);

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

		if (response == null) {
			return createErrorResult("No profile data returned from AI service");
		}

		// Determine document type and category based on the data extracted
		if (response.getPassport() != null && !response.getPassport().isEmpty()) {
			result.put("documentType", "IDENTITY");
			result.put("documentCategory", "PROFILE");
			result.put("extractedData", response.getPassport());
			result.put("isComplete", true);
		}
		else if (response.getUtilityBill() != null && !response.getUtilityBill().isEmpty()) {
			result.put("documentType", "ADDRESS_PROOF");
			result.put("documentCategory", "PROFILE");
			result.put("extractedData", response.getUtilityBill());
			result.put("isComplete", true);
		}
		else if (response.getP60() != null && !response.getP60().isEmpty()) {
			result.put("documentType", "FINANCIAL");
			result.put("documentCategory", "PROFILE");
			result.put("extractedData", response.getP60());
			result.put("isComplete", true);
		}
		else if (response.getParentsInfo() != null && !response.getParentsInfo().isEmpty()) {
			result.put("documentType", "APPLICANT");
			result.put("documentCategory", "PROFILE");
			result.put("extractedData", response.getRefereeInfo());
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