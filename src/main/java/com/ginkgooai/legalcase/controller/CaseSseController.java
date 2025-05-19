package com.ginkgooai.legalcase.controller;

import com.ginkgooai.legalcase.dto.LegalCaseResponse;
import com.ginkgooai.legalcase.service.CaseEventEmitterService;
import com.ginkgooai.legalcase.service.CaseService;
import com.ginkgooai.legalcase.service.EventLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * Case SSE controller
 */
@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Case SSE", description = "Server-sent events API for real-time case updates")
public class CaseSseController {

	private final EventLogService eventLogService;

	private final CaseService caseService;

	private final CaseEventEmitterService eventEmitterService;

	/**
	 * Provide SSE stream for real-time case update
	 * @param caseId Case ID
	 * @return SSE emitter
	 */
	@GetMapping(value = "/{caseId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@Operation(summary = "Stream case events", description = "Provides a Server-Sent stream for real-time case updates")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "SSE stream established successfully",
					content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)),
			@ApiResponse(responseCode = "404", description = "Case not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public SseEmitter streamCaseEvents(@Parameter(description = "ID of the case to stream events for",
			required = true) @PathVariable String caseId) {
		log.info("Client connecting to events stream for case: {}", caseId);

		SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

		eventEmitterService.addEmitter(caseId, emitter);

		try {
			LegalCaseResponse legalCase = caseService.getLegalCase(caseId);
			emitter.send(SseEmitter.event().name("init").data(legalCase));
		}
		catch (IOException e) {
			log.error("Error sending initial events", e);
			emitter.completeWithError(e);
		}

		return emitter;
	}

}