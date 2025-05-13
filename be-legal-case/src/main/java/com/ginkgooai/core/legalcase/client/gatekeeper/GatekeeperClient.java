package com.ginkgooai.core.legalcase.client.gatekeeper;

import com.ginkgooai.core.legalcase.client.gatekeeper.dto.QuestionnaireResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "gatekeeper-service", url = "${app.services.gatekeeper.url}")
public interface GatekeeperClient {

	@GetMapping("/api/v1/questionnaires/responses/user/{userId}")
	List<QuestionnaireResponseDTO> getQuestionnaireResponsesByUserId(@PathVariable("userId") String userId);

	@GetMapping("/api/v1/questionnaires/responses/{id}")
	QuestionnaireResponseDTO getQuestionnaireResponseById(@PathVariable("id") String id);

	@PostMapping("/api/v1/questionnaires/responses")
	QuestionnaireResponseDTO saveQuestionnaireResponse(@RequestBody QuestionnaireResponseDTO responseDTO);

}