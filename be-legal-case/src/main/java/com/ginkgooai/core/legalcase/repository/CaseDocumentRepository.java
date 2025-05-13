package com.ginkgooai.core.legalcase.repository;

import com.ginkgooai.core.legalcase.domain.CaseDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaseDocumentRepository extends JpaRepository<CaseDocument, String> {

	@Query("SELECT d FROM CaseDocument d WHERE d.legalCase.id = :caseId")
	Page<CaseDocument> findByLegalCaseId(@Param("caseId") String caseId, Pageable pageable);

	@Query("SELECT d FROM CaseDocument d WHERE d.legalCase.id = :caseId AND d.id = :documentId")
	Optional<CaseDocument> findByCaseIdAndDocumentId(@Param("caseId") String caseId,
			@Param("documentId") String documentId);

	@Query("SELECT COUNT(cd) FROM CaseDocument cd WHERE cd.legalCase.id = :caseId")
	long countByCaseId(@Param("caseId") String caseId);

}