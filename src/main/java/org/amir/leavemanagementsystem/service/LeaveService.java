package org.amir.leavemanagementsystem.service;

import org.amir.leavemanagementsystem.model.Leave;
import org.amir.leavemanagementsystem.model.LeaveStatus;
import org.amir.leavemanagementsystem.model.LeaveType;
import org.amir.leavemanagementsystem.model.User;

import java.util.List;

public interface LeaveService {
    Leave createLeave(Leave leave);
    Leave createLeaveForUser(Leave leave, Long userId);
    Leave getLeave(Long id);
    List<Leave> findByUserAndLeaveTypeAndStatus(User user, LeaveType type, LeaveStatus status);
    List<Leave> findApprovedLeavesByDepartmentAndDate(Long departmentId, LeaveStatus status);
    List<Leave> getAllLeaves();
    List<Leave> getLeavesByUser(Long userId);
    List<Leave> getLeavesByStatus(LeaveStatus status);
    List<Leave> getLeavesByType(LeaveType leaveType);
    List<Leave> getLeavesByDepartment(Long departmentId);
    List<Leave> getLeavesByDepartmentAndStatus(Long departmentId, LeaveStatus status);
    void cancelLeave(Long id);
    Leave updateLeave(Long id, Leave leave);
    void deleteLeave(Long id);
    Leave respondToLeave(Long id, LeaveStatus status, String comment, Long responderId);
} 