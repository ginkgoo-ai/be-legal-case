package com.ginkgooai.legalcase.service.impl;

import com.ginkgooai.legalcase.domain.event.DomainEvent;
import com.ginkgooai.legalcase.dto.EventLogResponse;
import com.ginkgooai.legalcase.dto.LegalCaseResponse;
import com.ginkgooai.legalcase.service.CaseEventEmitterService;
import com.ginkgooai.legalcase.service.CaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class CaseEventEmitterServiceImpl implements CaseEventEmitterService {

	private final CaseService caseService;

	private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

	public CaseEventEmitterServiceImpl(@Lazy CaseService caseService) {
		this.caseService = caseService;
	}

	@Override
	public SseEmitter addEmitter(String caseId, SseEmitter emitter) {
		log.debug("Adding emitter for case: {}", caseId);
		emitters.computeIfAbsent(caseId, k -> new CopyOnWriteArrayList<>()).add(emitter);

		emitter.onCompletion(() -> {
			log.debug("Emitter completed for case: {}", caseId);
			removeEmitter(caseId, emitter);
		});

		emitter.onTimeout(() -> {
			log.debug("Emitter timed out for case: {}", caseId);
			removeEmitter(caseId, emitter);
		});

		emitter.onError(e -> {
			log.error("Emitter error for case: {}", caseId, e);
			removeEmitter(caseId, emitter);
		});

		return emitter;
	}

	@Override
	public void removeEmitter(String caseId, SseEmitter emitter) {
		List<SseEmitter> caseEmitters = emitters.get(caseId);
		if (caseEmitters != null) {
			caseEmitters.remove(emitter);
			if (caseEmitters.isEmpty()) {
				emitters.remove(caseId);
			}
		}
	}

	@Override
	public void sendCaseUpdate(String caseId, LegalCaseResponse caseResponse) {
		List<SseEmitter> caseEmitters = emitters.get(caseId);
		if (caseEmitters != null && !caseEmitters.isEmpty()) {
			log.debug("Sending case update to {} emitters for case: {}", caseEmitters.size(), caseId);

			List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
			caseEmitters.forEach(emitter -> {
				try {
					emitter.send(SseEmitter.event().name("caseUpdate").data(caseResponse));
				}
				catch (IOException e) {
					log.error("Error sending case update to emitter", e);
					deadEmitters.add(emitter);
				}
			});

			deadEmitters.forEach(emitter -> removeEmitter(caseId, emitter));
		}
	}

	@Override
	public void sendEventUpdate(String caseId, EventLogResponse eventResponse) {
		List<SseEmitter> caseEmitters = emitters.get(caseId);
		if (caseEmitters != null && !caseEmitters.isEmpty()) {
			log.debug("Sending event update to {} emitters for case: {}", caseEmitters.size(), caseId);

			List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
			caseEmitters.forEach(emitter -> {
				try {
					emitter.send(SseEmitter.event().name("eventUpdate").data(eventResponse));
				}
				catch (IOException e) {
					log.error("Error sending event update to emitter", e);
					deadEmitters.add(emitter);
				}
			});

			deadEmitters.forEach(emitter -> removeEmitter(caseId, emitter));
		}
	}

	@Override
	public void handleDomainEvent(DomainEvent event) {
		String caseId = event.getCaseId();

		if (caseId == null) {
			log.warn("Cannot determine case ID for event: {}", event.getEventType());
			return;
		}

		if (emitters.containsKey(caseId)) {
			try {
				LegalCaseResponse caseResponse = caseService.getLegalCase(caseId);
				sendCaseUpdate(caseId, caseResponse);
			}
			catch (Exception e) {
				log.error("Error sending case update for event: {}", event.getEventType(), e);
			}
		}
	}

}