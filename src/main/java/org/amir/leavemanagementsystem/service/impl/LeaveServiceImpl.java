package org.amir.leavemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.amir.leavemanagementsystem.dto.LeaveRequest;
import org.amir.leavemanagementsystem.exception.BadRequestException;
import org.amir.leavemanagementsystem.exception.ResourceNotFoundException;
import org.amir.leavemanagementsystem.model.*;
import org.amir.leavemanagementsystem.repository.LeaveRepository;
import org.amir.leavemanagementsystem.repository.UserRepository;
import org.amir.leavemanagementsystem.service.EmailService;
import org.amir.leavemanagementsystem.service.LeaveService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public Leave createLeave(Leave leave) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Get the user from the repository
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Always set the user to the logged-in user, ignoring any user that might have been set in the leave object
        leave.setUser(currentUser);
        
        // Check if the user has MANAGER or ADMIN role
        boolean isManagerOrAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || 
                             a.getAuthority().equals("ROLE_ADMIN"));
        
        // Set the status based on the user's role
        if (isManagerOrAdmin) {
            leave.setStatus(LeaveStatus.APPROVED);
        } else {
            leave.setStatus(LeaveStatus.PENDING);
        }
        
        // Calculate duration
        leave.setDuration((int) java.time.temporal.ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1);
        
        // Validate annual leave limit for personal time off
        if (leave.getLeaveType() == LeaveType.PTO) {
            validateAnnualLeaveLimit(currentUser, leave);
        }
        
        // Validate reason for OTHER leave type
        if (leave.getLeaveType() == LeaveType.OTHER && 
            (leave.getReason() == null || leave.getReason().trim().isEmpty())) {
            throw new BadRequestException("Reason is required for OTHER leave type");
        }
        
        Leave savedLeave = leaveRepository.save(leave);
        
        // Send email notifications
        if (isManagerOrAdmin) {
            // For managers/admins, send approval notification
            emailService.sendLeaveApprovalNotification(currentUser, savedLeave);
        } else {
            // For regular users, send request notification
            emailService.sendLeaveRequestNotification(currentUser, savedLeave);
            
            // Send notification to managers for approval
            // In a real application, you would get the actual manager emails
            List<User> managers = userRepository.findByRoleIn(Arrays.asList(Role.MANAGER, Role.ADMIN));
            String[] managerEmails = managers.stream()
                    .map(User::getEmail)
                    .toArray(String[]::new);
            emailService.sendLeaveRequestToManagers(currentUser, savedLeave, managerEmails);
        }
        
        return savedLeave;
    }
    
    @Override
    @Transactional
    public Leave createLeaveForUser(Leave leave, Long userId) {
        // Get the user from the repository using the provided userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Set the user for the leave request to the specified user
        leave.setUser(user);
        
        // Set the status to APPROVED since this is an admin-manual recording
        leave.setStatus(LeaveStatus.APPROVED);
        
        // Calculate duration
        leave.setDuration((int) java.time.temporal.ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1);
        
        // Validate annual leave limit for personal time off
        if (leave.getLeaveType() == LeaveType.PTO) {
            validateAnnualLeaveLimit(user, leave);
        }
        
        // Validate reason for OTHER leave type
        if (leave.getLeaveType() == LeaveType.OTHER && 
            (leave.getReason() == null || leave.getReason().trim().isEmpty())) {
            throw new BadRequestException("Reason is required for OTHER leave type");
        }
        
        Leave savedLeave = leaveRepository.save(leave);
        
        // Send approval notification to the user
        emailService.sendLeaveApprovalNotification(user, savedLeave);
        
        return savedLeave;
    }

    @Override
    public Leave getLeave(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + id));
    }

    @Override
    public List<Leave> findByUserAndLeaveTypeAndStatus(User user, LeaveType type, LeaveStatus status) {
        return leaveRepository.findByUserAndLeaveTypeAndStatus(user, type, status);
    }

    @Override
    public List<Leave> findApprovedLeavesByDepartmentAndDate(Long departmentId, LeaveStatus status) {
        LocalDate currentDate = LocalDate.now();
        return leaveRepository.findApprovedLeavesByDepartmentAndDate(departmentId, status, currentDate);
    }

    @Override
    public List<Leave> getAllLeaves() {
        return leaveRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public void cancelLeave(Long id) {
        Leave leave = getLeave(id);
        User currentUser = getCurrentUser();

        if (!leave.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only cancel your own leave requests");
        }

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("You can only cancel PENDING leave requests");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        leaveRepository.save(leave);
    }

    @Override
    public List<Leave> getLeavesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return leaveRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Leave> getLeavesByStatus(LeaveStatus status) {
        return leaveRepository.findByStatus(status);
    }

    @Override
    public List<Leave> getLeavesByType(LeaveType leaveType) {
        return leaveRepository.findByLeaveType(leaveType);
    }

    @Override
    public List<Leave> getLeavesByDepartment(Long departmentId) {
        return leaveRepository.findByUserDepartmentId(departmentId);
    }

    @Override
    public List<Leave> getLeavesByDepartmentAndStatus(Long departmentId, LeaveStatus status) {
        return leaveRepository.findByUserDepartmentIdAndStatus(departmentId, status);
    }

    @Override
    @Transactional
    public Leave updateLeave(Long id, Leave leaveDetails) {
        Leave leave = getLeave(id);
        
        leave.setStartDate(leaveDetails.getStartDate());
        leave.setEndDate(leaveDetails.getEndDate());
        leave.setReason(leaveDetails.getReason());
        
        return leaveRepository.save(leave);
    }

    @Override
    @Transactional
    public void deleteLeave(Long id) {
        Leave leave = getLeave(id);
        User currentUser = getCurrentUser();

        if (!leave.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only delete your own leave requests");
        }

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("You can only delete PENDING leave requests");
        }

        leaveRepository.delete(leave);
    }

    @Override
    @Transactional
    public Leave respondToLeave(Long id, LeaveStatus status, String comment, Long responderId) {
        Leave leave = getLeave(id);
        User responder = userRepository.findById(responderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + responderId));
        
        // Validate that a comment is provided when rejecting a leave request
        if (status == LeaveStatus.REJECTED && (comment == null || comment.trim().isEmpty())) {
            throw new BadRequestException("A comment is required when rejecting a leave request");
        }
        
        leave.setStatus(status);
        leave.setRespondedBy(responder);
        leave.setResponseDate(LocalDateTime.now());
        leave.setComment(comment);
        
        Leave updatedLeave = leaveRepository.save(leave);
        
        // Send email notification based on the response
        if (status == LeaveStatus.APPROVED) {
            emailService.sendLeaveApprovalNotification(leave.getUser(), updatedLeave);
        } else if (status == LeaveStatus.REJECTED) {
            emailService.sendLeaveRejectionNotification(leave.getUser(), updatedLeave, comment);
        }
        
        return updatedLeave;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateLeaveRequest(LeaveRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date must be in the future");
        }
    }

    /**
     * Validates that the user hasn't exceeded their annual leave limit of 20 days
     */
    private void validateAnnualLeaveLimit(User user, Leave newLeave) {
        // Get the current year
        int currentYear = LocalDate.now().getYear();
        
        // Get all approved PTO leaves for the user in the current year
        List<Leave> approvedPTOLeaves = leaveRepository.findByUserAndLeaveTypeAndStatusAndStartDateYear(
                user, LeaveType.PTO, LeaveStatus.APPROVED, currentYear);
        
        // Calculate total days of approved PTO
        int totalApprovedDays = approvedPTOLeaves.stream()
                .mapToInt(Leave::getDuration)
                .sum();
        
        // Add the days from the new leave request
        totalApprovedDays += newLeave.getDuration();
        
        // Check if the total exceeds the annual limit
        if (totalApprovedDays > 20) {
            throw new BadRequestException("You have exceeded the annual limit of 20 days for personal time off. " +
                    "You have already used " + (totalApprovedDays - newLeave.getDuration()) + " days this year.");
        }
    }
} 