package com.vinncorp.erp.modules.hr.repository;

import com.vinncorp.erp.modules.hr.entity.HrHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HrHolidayRepository extends JpaRepository<HrHoliday, Long> {

    Optional<HrHoliday> findByIdAndWorkspaceId(Long id, Long workspaceId);

    List<HrHoliday> findAllByWorkspaceIdOrderByHolidayDateDesc(Long workspaceId);

    List<HrHoliday> findByHolidayDateAndWorkspaceId(LocalDate date, Long workspaceId);

    List<HrHoliday> findByHolidayDateBetweenAndWorkspaceId(LocalDate startDate, LocalDate endDate, Long workspaceId);

    boolean existsByNameAndHolidayDateAndWorkspaceId(String name, LocalDate holidayDate, Long workspaceId);
}
