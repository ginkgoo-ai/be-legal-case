package com.ginkgooai.legalcase.repository;

import com.ginkgooai.legalcase.domain.CaseDocument;
import com.ginkgooai.legalcase.domain.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for case documents
 */
@Repository
public interface CaseDocumentRepository extends JpaRepository<CaseDocument, String> {

	/**
	 * Find all documents for a case with pagination
	 * @param caseId Case ID
	 * @param pageable Pagination parameters
	 * @return Page of documents
	 */
	@Query("SELECT d FROM CaseDocument d WHERE d.legalCase.id = :caseId")
	Page<CaseDocument> findByCaseId(@Param("caseId") String caseId, Pageable pageable);

	/**
	 * Find all documents for a case
	 * @param caseId Case ID
	 * @return List of documents
	 */
	@Query("SELECT d FROM CaseDocument d WHERE d.legalCase.id = :caseId")
	List<CaseDocument> findAllByCaseId(@Param("caseId") String caseId);

	/**
	 * Find a specific document in a case
	 * @param caseId Case ID
	 * @param documentId Document ID
	 * @return The document, if found
	 */
	@Query("SELECT d FROM CaseDocument d WHERE d.legalCase.id = :caseId AND d.id = :documentId")
	Optional<CaseDocument> findByCaseIdAndDocumentId(@Param("caseId") String caseId,
			@Param("documentId") String documentId);

	/**
	 * Count documents for a case
	 * @param caseId Case ID
	 * @return Number of documents
	 */
	@Query("SELECT COUNT(d) FROM CaseDocument d WHERE d.legalCase.id = :caseId")
	long countByCaseId(@Param("caseId") String caseId);

	/**
	 * Find documents by type for a case
	 * @param caseId Case ID
	 * @param documentType Document type
	 * @return List of documents
	 */
	@Query("SELECT d FROM CaseDocument d WHERE d.legalCase.id = :caseId AND d.documentType = :documentType")
	List<CaseDocument> findByCaseIdAndDocumentType(@Param("caseId") String caseId,
			@Param("documentType") DocumentType documentType);

	/**
	 * Find document by ID with legal case eagerly loaded
	 * @param documentId Document ID
	 * @return The document with eagerly loaded legal case, if found
	 */
	@Query("SELECT d FROM CaseDocument d JOIN FETCH d.legalCase WHERE d.id = :documentId")
	Optional<CaseDocument> findByIdWithLegalCase(@Param("documentId") String documentId);

	/**
	 * Find documents by storage ID for a case
	 * @param caseId Case ID
	 * @param storageId Storage ID
	 * @return List of documents
	 */
	@Query("SELECT d FROM CaseDocument d WHERE d.legalCase.id = :caseId AND d.storageId = :storageId")
	List<CaseDocument> findByCaseIdAndStorageId(@Param("caseId") String caseId, @Param("storageId") String storageId);

}