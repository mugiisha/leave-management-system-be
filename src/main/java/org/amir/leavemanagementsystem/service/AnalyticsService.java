package org.amir.leavemanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.amir.leavemanagementsystem.dto.AnalyticsResponse;
import org.amir.leavemanagementsystem.model.Leave;
import org.amir.leavemanagementsystem.model.LeaveStatus;
import org.amir.leavemanagementsystem.model.LeaveType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private LeaveService leaveService;


    public AnalyticsService(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    public AnalyticsResponse getAnalytics(Long userId) {
        AnalyticsResponse analyticsResponse = new AnalyticsResponse();

        List<Leave> userLeaves= leaveService.getLeavesByUser(userId);

        analyticsResponse.setTotalLeaveRequests(userLeaves.size());


        // Fetch total leave requests
        int totalApprovedDays = userLeaves.stream()
                .mapToInt(Leave::getDuration)
                .sum();;

        analyticsResponse.setPtoBalance(totalApprovedDays);

        return analyticsResponse;
    }
}
