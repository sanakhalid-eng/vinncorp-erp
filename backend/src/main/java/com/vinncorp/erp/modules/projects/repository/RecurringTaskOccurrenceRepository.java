package com.vinncorp.erp.modules.projects.repository;

import com.vinncorp.erp.modules.projects.entity.RecurringTaskOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringTaskOccurrenceRepository extends JpaRepository<RecurringTaskOccurrence, Long> {

    List<RecurringTaskOccurrence> findByRecurringTemplateIdOrderByOccurrenceDateAsc(Long recurringTemplateId);

    Optional<RecurringTaskOccurrence> findByRecurringTemplateIdAndOccurrenceDate(Long recurringTemplateId, LocalDate occurrenceDate);

    boolean existsByRecurringTemplateIdAndOccurrenceDate(Long recurringTemplateId, LocalDate occurrenceDate);

    List<RecurringTaskOccurrence> findByGeneratedTaskId(Long generatedTaskId);

    long countByRecurringTemplateId(Long recurringTemplateId);

    boolean existsByGeneratedTaskId(Long generatedTaskId);
}



