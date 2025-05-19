package com.ginkgooai.legalcase.service.ai;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Service for analyzing documents using AI
 */
public interface DocumentAnalysisService {

	/**
	 * Analyze a document to determine its type, category, and extract data
	 * @param documentId the document ID
	 * @param documentUrl URL of the document to analyze
	 * @param callback callback function to process analysis results
	 */
	void analyzeDocument(String caseId, String documentId, String documentUrl,
			BiConsumer<Pair, Map<String, Object>> callback);

	/**
	 * Analyze a document synchronously
	 * @param documentUrl URL of the document to analyze
	 * @return analysis results including document type, category, and extracted data
	 */
	Map<String, Object> analyzeDocumentSync(String documentUrl);

}