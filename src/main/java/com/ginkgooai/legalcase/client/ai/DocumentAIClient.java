package com.ginkgooai.legalcase.client.ai;

import com.ginkgooai.core.common.config.FeignConfig;
import com.ginkgooai.legalcase.client.ai.dto.DocumentParseRequest;
import com.ginkgooai.legalcase.client.ai.dto.DocumentParseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Client for interacting with the Python AI document parsing service
 */
@FeignClient(name = "legal-ai-service", url = "${core-ai-uri}", configuration = FeignConfig.class)
public interface DocumentAIClient {

	/**
	 * Parse document files into structured profile data
	 * @param request the document parse request containing file URLs
	 * @return the parsed profile data
	 */
	@PostMapping("/files/structure")
	DocumentParseResponse parseDocuments(@RequestBody DocumentParseRequest request);

}