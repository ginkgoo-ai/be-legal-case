package com.ginkgooai.legalcase.repository;

import com.ginkgooai.legalcase.domain.event.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for event logs
 */
@Repository
public interface EventLogRepository extends JpaRepository<EventLog, String> {

	/**
	 * Find all event logs for a case, ordered by occurred time
	 * @param caseId Case ID
	 * @return List of event logs
	 */
	List<EventLog> findByLegalCaseIdOrderByOccurredAtAsc(String caseId);

	/**
	 * Find all event logs for a case and event type, ordered by occurred time
	 * @param caseId Case ID
	 * @param eventType Event type
	 * @return List of event logs
	 */
	List<EventLog> findByLegalCaseIdAndEventTypeOrderByOccurredAtAsc(String caseId, String eventType);

	/**
	 * Find the last LLM analysis initiated event for a case
	 * @param caseId Case ID
	 * @param eventType Event type
	 * @return Last LLM analysis event
	 */
	@Query("SELECT el FROM EventLog el WHERE el.legalCase.id = :caseId AND el.eventType = :eventType ORDER BY el.occurredAt DESC")
	List<EventLog> findLastEventByTypeAndCaseId(String caseId, String eventType);

	@Query("SELECT MAX(el.occurredAt) FROM EventLog el WHERE el.legalCase.id = :caseId AND el.eventType = :eventType")
	Optional<LocalDateTime> findLastEventTimeByTypeAndCaseId(String caseId, String eventType);

}