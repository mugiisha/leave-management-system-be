package org.amir.leavemanagementsystem.repository;

import org.amir.leavemanagementsystem.model.Leave;
import org.amir.leavemanagementsystem.model.LeaveStatus;
import org.amir.leavemanagementsystem.model.LeaveType;
import org.amir.leavemanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByUser(User user);
    List<Leave> findByUserOrderByCreatedAtDesc(User user);
    List<Leave> findByUserAndLeaveTypeAndStatus(User user, LeaveType leaveType, LeaveStatus status);
    List<Leave> findByStatus(LeaveStatus status);
    List<Leave> findByLeaveType(LeaveType leaveType);
    List<Leave> findAllByOrderByCreatedAtDesc();
    @Query("SELECT l FROM Leave l WHERE l.user.department.id = :departmentId AND l.status = :status AND :currentDate BETWEEN l.startDate AND l.endDate")
    List<Leave> findApprovedLeavesByDepartmentAndDate(@Param("departmentId") Long departmentId, @Param("status") LeaveStatus status, @Param("currentDate") LocalDate currentDate);
    @Query("SELECT l FROM Leave l WHERE l.user.department.id = :departmentId")
    List<Leave> findByUserDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT l FROM Leave l WHERE l.user.department.id = :departmentId AND l.status = :status")
    List<Leave> findByUserDepartmentIdAndStatus(@Param("departmentId") Long departmentId, @Param("status") LeaveStatus status);

    @Query("SELECT l FROM Leave l WHERE l.user = :user AND l.leaveType = :leaveType AND l.status = :status AND YEAR(l.startDate) = :year")
    List<Leave> findByUserAndLeaveTypeAndStatusAndStartDateYear(@Param("user") User user, @Param("leaveType") LeaveType leaveType, @Param("status") LeaveStatus status, @Param("year") int year);
} 