package com.vinncorp.erp.integration;

import com.vinncorp.erp.AbstractIntegrationTest;
import com.vinncorp.erp.modules.hr.entity.Employee;
import com.vinncorp.erp.modules.hr.entity.HrLeaveType;
import com.vinncorp.erp.modules.hr.enums.LeaveRequestStatus;
import com.vinncorp.erp.modules.hr.repository.EmployeeRepository;
import com.vinncorp.erp.modules.hr.repository.HrLeaveBalanceRepository;
import com.vinncorp.erp.modules.hr.repository.HrLeaveRequestRepository;
import com.vinncorp.erp.modules.hr.repository.HrLeaveTypeRepository;
import com.vinncorp.erp.modules.hr.dto.request.LeaveRequestCreateRequest;
import com.vinncorp.erp.modules.hr.dto.request.LeaveTypeCreateRequest;
import com.vinncorp.erp.modules.hr.service.LeaveBalanceService;
import com.vinncorp.erp.modules.hr.service.LeaveRequestService;
import com.vinncorp.erp.modules.hr.service.LeaveTypeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LeaveManagementIntegrationTest extends AbstractIntegrationTest {

    @Autowired private LeaveTypeService leaveTypeService;
    @Autowired private LeaveRequestService leaveRequestService;
    @Autowired private LeaveBalanceService leaveBalanceService;
    @Autowired private HrLeaveTypeRepository leaveTypeRepository;
    @Autowired private HrLeaveRequestRepository leaveRequestRepository;
    @Autowired private HrLeaveBalanceRepository leaveBalanceRepository;
    @Autowired private EmployeeRepository employeeRepository;

    private Employee testEmployee;
    private Employee managerEmployee;
    private HrLeaveType annualLeaveType;

    private LocalDate nextMonday() {
        LocalDate d = LocalDate.now();
        while (d.getDayOfWeek() != DayOfWeek.MONDAY) {
            d = d.plusDays(1);
        }
        return d;
    }

    @BeforeEach
    void setUp() {
        leaveRequestRepository.deleteAll();
        leaveBalanceRepository.deleteAll();
        leaveTypeRepository.deleteAll();
        employeeRepository.deleteAll();

        managerEmployee = new Employee();
        managerEmployee.setEmployeeCode("MGR001");
        managerEmployee.setFirstName("Manager");
        managerEmployee.setLastName("User");
        managerEmployee.setWorkEmail("manager@test.com");
        managerEmployee.setHireDate(LocalDate.of(2023, 1, 1));
        managerEmployee.setWorkspace(testWorkspace);
        managerEmployee = employeeRepository.save(managerEmployee);

        testEmployee = new Employee();
        testEmployee.setEmployeeCode("EMP001");
        testEmployee.setFirstName("Test");
        testEmployee.setLastName("Employee");
        testEmployee.setWorkEmail("test.employee@test.com");
        testEmployee.setHireDate(LocalDate.of(2024, 1, 1));
        testEmployee.setWorkspace(testWorkspace);
        testEmployee.setManagerId(managerEmployee.getId());
        testEmployee = employeeRepository.save(testEmployee);

        HrLeaveType lt = new HrLeaveType();
        lt.setCode("ANNUAL");
        lt.setName("Annual Leave");
        lt.setDefaultDays(20);
        lt.setIsPaid(true);
        lt.setIsActive(true);
        lt.setWorkspace(testWorkspace);
        annualLeaveType = leaveTypeRepository.save(lt);
    }

    @Test
    void testCreateLeaveType() {
        var result = leaveTypeService.create(
                LeaveTypeCreateRequest.builder()
                        .name("Sick Leave").code("SICK").defaultDays(10).isPaid(true).build(),
                testWorkspace.getId());

        assertNotNull(result);
        assertEquals("Sick Leave", result.getName());
        assertEquals(10, result.getDefaultDays());
    }

    @Test
    void testApplyLeave() {
        LocalDate start = nextMonday();
        var result = leaveRequestService.apply(
                LeaveRequestCreateRequest.builder()
                        .employeeId(testEmployee.getId())
                        .leaveTypeId(annualLeaveType.getId())
                        .startDate(start)
                        .endDate(start.plusDays(4))
                        .reason("Vacation")
                        .build(),
                testWorkspace.getId(), adminUser.getId());

        assertNotNull(result);
        assertEquals(LeaveRequestStatus.PENDING.name(), result.getStatus());
        assertEquals(5, result.getTotalDays().intValue());
    }

    @Test
    void testApproveLeave() {
        LocalDate start = nextMonday().plusWeeks(3);
        var applied = leaveRequestService.apply(
                LeaveRequestCreateRequest.builder()
                        .employeeId(testEmployee.getId())
                        .leaveTypeId(annualLeaveType.getId())
                        .startDate(start)
                        .endDate(start.plusDays(4))
                        .reason("Family trip")
                        .build(),
                testWorkspace.getId(), adminUser.getId());

        var approved = leaveRequestService.approve(applied.getId(), testWorkspace.getId(), adminUser.getId());

        assertEquals(LeaveRequestStatus.APPROVED.name(), approved.getStatus());
        assertNotNull(approved.getApprovedAt());
    }

    @Test
    void testRejectLeave() {
        LocalDate start = nextMonday().plusWeeks(4);
        var applied = leaveRequestService.apply(
                LeaveRequestCreateRequest.builder()
                        .employeeId(testEmployee.getId())
                        .leaveTypeId(annualLeaveType.getId())
                        .startDate(start)
                        .endDate(start.plusDays(2))
                        .reason("Personal")
                        .build(),
                testWorkspace.getId(), adminUser.getId());

        var rejected = leaveRequestService.reject(applied.getId(),
                com.vinncorp.erp.modules.hr.dto.request.LeaveRequestActionRequest.builder()
                        .rejectionReason("Insufficient balance").build(),
                testWorkspace.getId(), adminUser.getId());

        assertEquals(LeaveRequestStatus.REJECTED.name(), rejected.getStatus());
        assertEquals("Insufficient balance", rejected.getRejectionReason());
    }

    @Test
    void testCancelPendingLeave() {
        LocalDate start = nextMonday().plusWeeks(5);
        var applied = leaveRequestService.apply(
                LeaveRequestCreateRequest.builder()
                        .employeeId(testEmployee.getId())
                        .leaveTypeId(annualLeaveType.getId())
                        .startDate(start)
                        .endDate(start.plusDays(2))
                        .build(),
                testWorkspace.getId(), adminUser.getId());

        var cancelled = leaveRequestService.cancel(applied.getId(), testWorkspace.getId(), adminUser.getId());

        assertEquals(LeaveRequestStatus.CANCELLED.name(), cancelled.getStatus());
        assertNotNull(cancelled.getCancelledAt());
    }

    @Test
    void testDuplicateOverlappingLeaveRejected() {
        LocalDate start = nextMonday().plusWeeks(6);
        leaveRequestService.apply(
                LeaveRequestCreateRequest.builder()
                        .employeeId(testEmployee.getId())
                        .leaveTypeId(annualLeaveType.getId())
                        .startDate(start)
                        .endDate(start.plusDays(4))
                        .reason("First request")
                        .build(),
                testWorkspace.getId(), adminUser.getId());

        assertThrows(com.vinncorp.erp.shared.exception.ConflictException.class,
                () -> leaveRequestService.apply(
                        LeaveRequestCreateRequest.builder()
                                .employeeId(testEmployee.getId())
                                .leaveTypeId(annualLeaveType.getId())
                                .startDate(start.plusDays(2))
                                .endDate(start.plusDays(6))
                                .reason("Overlapping")
                                .build(),
                        testWorkspace.getId(), adminUser.getId()));
    }

    @Test
    void testSeedAndGetLeaveBalance() {
        var seeded = leaveBalanceService.seedBalance(
                com.vinncorp.erp.modules.hr.dto.request.LeaveBalanceSeedRequest.builder()
                        .employeeId(testEmployee.getId())
                        .leaveTypeId(annualLeaveType.getId())
                        .year(LocalDate.now().getYear())
                        .totalDays(BigDecimal.valueOf(20))
                        .build(),
                testWorkspace.getId());

        assertNotNull(seeded);
        assertEquals(20, seeded.getTotalDays().intValue());

        var balances = leaveBalanceService.getBalancesByEmployee(
                testEmployee.getId(), LocalDate.now().getYear(), testWorkspace.getId());

        assertFalse(balances.isEmpty());
    }

    @Test
    void testPendingCount() {
        LocalDate start = nextMonday().plusWeeks(7);
        leaveRequestService.apply(
                LeaveRequestCreateRequest.builder()
                        .employeeId(testEmployee.getId())
                        .leaveTypeId(annualLeaveType.getId())
                        .startDate(start)
                        .endDate(start.plusDays(2))
                        .build(),
                testWorkspace.getId(), adminUser.getId());

        long pendingCount = leaveRequestService.countPending(testWorkspace.getId());
        assertTrue(pendingCount >= 1);
    }
}
