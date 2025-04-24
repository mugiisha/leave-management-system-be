package org.amir.leavemanagementsystem.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.amir.leavemanagementsystem.dto.ApiResponse;
import org.amir.leavemanagementsystem.dto.LeaveRequest;
import org.amir.leavemanagementsystem.exception.ResourceNotFoundException;
import org.amir.leavemanagementsystem.model.Leave;
import org.amir.leavemanagementsystem.model.LeaveStatus;
import org.amir.leavemanagementsystem.model.LeaveType;
import org.amir.leavemanagementsystem.model.User;
import org.amir.leavemanagementsystem.repository.UserRepository;
import org.amir.leavemanagementsystem.service.LeaveService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final UserRepository userRepository;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Leave>> requestLeave(@Valid @RequestBody LeaveRequest request,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        Leave leave = new Leave();
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setLeaveType(request.getLeaveType());
        leave.setReason(request.getReason());
        
        Leave createdLeave = leaveService.createLeave(leave);
        return ResponseEntity.ok(ApiResponse.success(createdLeave, "Your leave request has been submitted and is pending approval"));
    }
    
    @PostMapping("/record")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Leave>> recordLeave(@Valid @RequestBody LeaveRequest request,
                                                        @RequestParam Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        Leave leave = new Leave();
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setLeaveType(request.getLeaveType());
        leave.setReason(request.getReason());

        // Set the respondedBy field to the authenticated user
        String email = userDetails.getUsername();
        User responder = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        leave.setRespondedBy(responder);
        
        Leave createdLeave = leaveService.createLeaveForUser(leave, userId);
        return ResponseEntity.ok(ApiResponse.success(createdLeave, "Leave has been recorded and automatically approved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Leave>> getLeave(@PathVariable Long id) {
        Leave leave = leaveService.getLeave(id);
        return ResponseEntity.ok(ApiResponse.success(leave));
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Leave>>> getAllLeaves() {
        List<Leave> leaves = leaveService.getAllLeaves();
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Leave>>> getLeavesByUser(@PathVariable Long userId) {
        List<Leave> leaves = leaveService.getLeavesByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Leave>>> getLeavesByStatus(@PathVariable LeaveStatus status) {
        List<Leave> leaves = leaveService.getLeavesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    @PutMapping("/cancel/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Void>> cancelLeave(@PathVariable Long id) {
        leaveService.cancelLeave(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Leave request cancelled successfully"));
    }

    @GetMapping("/type/{leaveType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<Leave>>> getLeavesByType(@PathVariable LeaveType leaveType) {
        List<Leave> leaves = leaveService.getLeavesByType(leaveType);
        return ResponseEntity.ok(ApiResponse.success(leaves, "Leaves retrieved successfully"));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Leave>>> getLeavesByDepartment(@PathVariable Long departmentId) {
        List<Leave> leaves = leaveService.getLeavesByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }
    
    @GetMapping("/department/{departmentId}/status/{status}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Leave>>> getLeavesByDepartmentAndStatus(
            @PathVariable Long departmentId,
            @PathVariable LeaveStatus status) {
        List<Leave> leaves = leaveService.getLeavesByDepartmentAndStatus(departmentId, status);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Leave>> updateLeave(@PathVariable Long id, @Valid @RequestBody LeaveRequest request) {
        Leave leave = new Leave();
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setLeaveType(request.getLeaveType());
        leave.setReason(request.getReason());
        
        Leave updatedLeave = leaveService.updateLeave(id, leave);
        return ResponseEntity.ok(ApiResponse.success(updatedLeave, "Leave request updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteLeave(@PathVariable Long id) {
        leaveService.deleteLeave(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Leave request deleted successfully"));
    }

    @PutMapping("/{id}/respond")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Leave>> respondToLeave(
            @PathVariable Long id,
            @RequestParam LeaveStatus status,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Get the responder's ID from the authenticated user
        String email = userDetails.getUsername();
        User responder = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Leave leave = leaveService.respondToLeave(id, status, comment, responder.getId());
        String message = status == LeaveStatus.APPROVED ? 
                "Leave request approved successfully" : 
                "Leave request rejected successfully";
        
        return ResponseEntity.ok(ApiResponse.success(leave, message));
    }
} 