package com.vinncorp.erp.modules.hr.service.impl;

import com.vinncorp.erp.modules.hr.entity.HrHoliday;
import com.vinncorp.erp.modules.hr.repository.HrHolidayRepository;
import com.vinncorp.erp.modules.hr.dto.request.HolidayCreateRequest;
import com.vinncorp.erp.modules.hr.dto.response.HolidayResponse;
import com.vinncorp.erp.modules.hr.service.HolidayService;
import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HolidayServiceImpl implements HolidayService {

    private final HrHolidayRepository holidayRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional
    public HolidayResponse create(HolidayCreateRequest request, Long workspaceId) {
        if (holidayRepository.existsByNameAndHolidayDateAndWorkspaceId(
                request.getName(), request.getHolidayDate(), workspaceId)) {
            throw new ConflictException("Holiday already exists for this date");
        }

        HrHoliday holiday = new HrHoliday();
        holiday.setName(request.getName());
        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setHolidayType(request.getHolidayType() != null ? request.getHolidayType() : "PUBLIC");
        holiday.setDescription(request.getDescription());
        holiday.setIsRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        holiday.setWorkspace(workspace);

        return HolidayResponse.from(holidayRepository.save(holiday));
    }

    @Override
    @Transactional
    public HolidayResponse update(Long id, HolidayCreateRequest request, Long workspaceId) {
        HrHoliday holiday = holidayRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found"));

        holiday.setName(request.getName());
        holiday.setHolidayDate(request.getHolidayDate());
        if (request.getHolidayType() != null) holiday.setHolidayType(request.getHolidayType());
        holiday.setDescription(request.getDescription());
        if (request.getIsRecurring() != null) holiday.setIsRecurring(request.getIsRecurring());

        return HolidayResponse.from(holidayRepository.save(holiday));
    }

    @Override
    public HolidayResponse get(Long id, Long workspaceId) {
        HrHoliday holiday = holidayRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found"));
        return HolidayResponse.from(holiday);
    }

    @Override
    public List<HolidayResponse> list(Long workspaceId) {
        return holidayRepository.findAllByWorkspaceIdOrderByHolidayDateDesc(workspaceId)
                .stream().map(HolidayResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<HolidayResponse> getByDateRange(Long workspaceId, LocalDate startDate, LocalDate endDate) {
        return holidayRepository.findByHolidayDateBetweenAndWorkspaceId(startDate, endDate, workspaceId)
                .stream().map(HolidayResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id, Long workspaceId) {
        HrHoliday holiday = holidayRepository.findByIdAndWorkspaceId(id, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found"));
        holidayRepository.delete(holiday);
    }
}
