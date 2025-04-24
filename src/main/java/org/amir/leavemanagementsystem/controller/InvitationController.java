package org.amir.leavemanagementsystem.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.amir.leavemanagementsystem.dto.ApiResponse;
import org.amir.leavemanagementsystem.dto.CompleteInvitationRequest;
import org.amir.leavemanagementsystem.dto.InvitationRequest;
import org.amir.leavemanagementsystem.dto.TokenRenewalRequest;
import org.amir.leavemanagementsystem.model.UserInvitation;
import org.amir.leavemanagementsystem.service.InvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {
    private final InvitationService invitationService;
    
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> inviteUser(@Valid @RequestBody InvitationRequest request) {
        invitationService.inviteUser(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Invitation sent successfully"));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserInvitation>>> getInvitations() {
        return ResponseEntity.ok(ApiResponse.success(invitationService.getInvitations(), "Invitations retrieved successfully"));
    }


    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> completeInvitation(@Valid @RequestBody CompleteInvitationRequest request) {
        invitationService.completeInvitation(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Account created successfully"));
    }
    
    @PostMapping("/renew")
    public ResponseEntity<ApiResponse<Void>> renewInvitationToken(@Valid @RequestBody TokenRenewalRequest request) {
        invitationService.renewInvitationToken(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Invitation token renewed successfully"));
    }
} 