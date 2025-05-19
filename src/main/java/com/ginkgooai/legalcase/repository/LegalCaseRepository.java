package com.ginkgooai.legalcase.repository;

import com.ginkgooai.legalcase.domain.CaseDocument;
import com.ginkgooai.legalcase.domain.CaseStatus;
import com.ginkgooai.legalcase.domain.LegalCase;
import org.hibernate.annotations.processing.Find;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing LegalCase entities
 */
@Repository
public interface LegalCaseRepository extends JpaRepository<LegalCase, String> {

	/**
	 * Find cases by profile ID
	 */
	Page<LegalCase> findByProfileId(String profileId, Pageable pageable);

	/**
	 * Find cases by client ID
	 */
	Page<LegalCase> findByClientId(String clientId, Pageable pageable);

	@Query("SELECT lc FROM LegalCase lc WHERE lc.profileId = :profileId AND lc.status = :status")
	List<LegalCase> findByProfileIdAndStatus(@Param("profileId") String profileId, @Param("status") CaseStatus status);

	/**
	 * Search cases by title or case number
	 */
	@Query("SELECT c FROM LegalCase c WHERE c.title LIKE %:searchTerm%")
	Page<LegalCase> searchCases(@Param("searchTerm") String searchTerm, Pageable pageable);

	long countByProfileId(String profileId);

	long countByClientId(String clientId);

	/**
	 * Find documents belonging to a case
	 */
	@Query("SELECT d FROM CaseDocument d WHERE d.legalCase.id = :caseId")
	List<CaseDocument> findDocumentsByCaseId(@Param("caseId") String caseId);

	/**
	 * Find a case by ID and eager load its documents
	 * @param caseId Case ID
	 * @return Optional containing the case with documents eagerly loaded
	 */
	@Query("SELECT lc FROM LegalCase lc LEFT JOIN FETCH lc.documents WHERE lc.id = :caseId")
	Optional<LegalCase> findByIdWithDocuments(@Param("caseId") String caseId);

}