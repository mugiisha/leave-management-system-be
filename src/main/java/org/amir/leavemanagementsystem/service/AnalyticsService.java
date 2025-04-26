package org.amir.leavemanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.amir.leavemanagementsystem.dto.AnalyticsResponse;
import org.amir.leavemanagementsystem.model.Leave;
import org.amir.leavemanagementsystem.model.LeaveStatus;
import org.amir.leavemanagementsystem.model.LeaveType;
import org.amir.leavemanagementsystem.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final LeaveService leaveService;
    private final UserService userService;

    public AnalyticsResponse getAnalytics(Long userId) {
        AnalyticsResponse analyticsResponse = new AnalyticsResponse();

        User user =userService.getUser(userId);

        List<Leave> userLeaves= leaveService.getLeavesByUser(userId);
        List<Leave> teamLeaves = leaveService.findApprovedLeavesByDepartmentAndDate(user.getDepartment().getId(), LeaveStatus.APPROVED);
        List<Leave> approvedLeaves = leaveService.findByUserAndLeaveTypeAndStatus(user, LeaveType.PTO, LeaveStatus.APPROVED);

        // Fetch total leave requests
        int totalApprovedDays = approvedLeaves.stream()
                .mapToInt(Leave::getDuration)
                .sum();;

        analyticsResponse.setTeamLeaves(teamLeaves);

        analyticsResponse.setTotalLeaveRequests(userLeaves.size());

        analyticsResponse.setPtoBalance(totalApprovedDays);

        return analyticsResponse;
    }
}
