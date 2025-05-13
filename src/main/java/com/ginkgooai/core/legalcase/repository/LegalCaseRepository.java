package com.ginkgooai.core.legalcase.repository;

import com.ginkgooai.core.legalcase.domain.LegalCase;
import com.ginkgooai.core.legalcase.domain.LegalCase.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegalCaseRepository extends JpaRepository<LegalCase, String> {

	Page<LegalCase> findByProfileId(String profileId, Pageable pageable);

	Page<LegalCase> findByClientId(String clientId, Pageable pageable);

	Optional<LegalCase> findByCaseNumber(String caseNumber);

	@Query("SELECT lc FROM LegalCase lc WHERE lc.profileId = :profileId AND lc.status = :status")
	List<LegalCase> findByProfileIdAndStatus(@Param("profileId") String profileId, @Param("status") CaseStatus status);

	@Query("SELECT lc FROM LegalCase lc WHERE " + "LOWER(lc.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
			+ "LOWER(lc.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
			+ "LOWER(lc.caseNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
	Page<LegalCase> searchCases(@Param("searchTerm") String searchTerm, Pageable pageable);

	long countByProfileId(String profileId);

	long countByClientId(String clientId);

}