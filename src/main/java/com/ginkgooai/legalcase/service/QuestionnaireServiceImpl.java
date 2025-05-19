package com.ginkgooai.legalcase.service;

import com.ginkgooai.legalcase.dto.QuestionnaireSubmissionRequest;
import com.ginkgooai.legalcase.dto.QuestionnaireSubmissionResponse;
import com.ginkgooai.legalcase.repository.LegalCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionnaireServiceImpl implements QuestionnaireService {

	private final LegalCaseRepository legalCaseRepository;

	private final CaseService legalCaseService;

	@Override
	@Transactional
	public QuestionnaireSubmissionResponse processQuestionnaireSubmission(QuestionnaireSubmissionRequest request) {
		return null;
	}

}
