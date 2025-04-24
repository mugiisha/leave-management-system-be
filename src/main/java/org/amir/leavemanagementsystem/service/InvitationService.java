package org.amir.leavemanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.amir.leavemanagementsystem.dto.CompleteInvitationRequest;
import org.amir.leavemanagementsystem.dto.InvitationRequest;
import org.amir.leavemanagementsystem.dto.TokenRenewalRequest;
import org.amir.leavemanagementsystem.exception.BadRequestException;
import org.amir.leavemanagementsystem.exception.ResourceNotFoundException;
import org.amir.leavemanagementsystem.model.Department;
import org.amir.leavemanagementsystem.model.Role;
import org.amir.leavemanagementsystem.model.User;
import org.amir.leavemanagementsystem.model.UserInvitation;
import org.amir.leavemanagementsystem.model.UserStatus;
import org.amir.leavemanagementsystem.repository.UserInvitationRepository;
import org.amir.leavemanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {
    private final UserInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    @Transactional
    public void inviteUser(InvitationRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User with this email already exists");
        }
        
        // Check if there's already an active invitation for this email
        if (invitationRepository.existsByEmailAndUsedFalse(request.getEmail())) {
            throw new BadRequestException("An active invitation already exists for this email");
        }
        
        // Generate a secure token
        String token = generateSecureToken();
        
        // Get department if provided
        Department department = null;
        if (request.getDepartment() != null) {
            department = departmentService.getDepartment(request.getDepartment());
        }
        
        // Create invitation
        UserInvitation invitation = UserInvitation.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .token(token)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .used(false)
                .role(request.getRole())
                .department(department)
                .build();
        
        invitation = invitationRepository.save(invitation);
        
        // Send invitation email
        String invitationLink = frontendUrl + "/set-password/" + token;
        emailService.sendInvitationEmail(invitation, invitationLink);
        
        log.info("Invitation sent to {}", request.getEmail());
    }
    
    @Transactional
    public void completeInvitation(CompleteInvitationRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        
        // Find invitation by token
        UserInvitation invitation = invitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token"));
        
        // Check if invitation is valid
        if (invitation.isUsed()) {
            throw new BadRequestException("Invitation has already been used");
        }
        
        if (invitation.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invitation has expired");
        }
        
        // Create user with the new password
        User user = User.builder()
                .email(invitation.getEmail())
                .firstName(invitation.getFirstName())
                .lastName(invitation.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(invitation.getRole())
                .department(invitation.getDepartment())
                .status(UserStatus.APPROVED)
                .build();
        
        userRepository.save(user);
        
        // Mark invitation as used
        invitation.setUsed(true);
        invitationRepository.save(invitation);
        
        log.info("User account created for {}", invitation.getEmail());
    }
    
    @Transactional
    public void renewInvitationToken(TokenRenewalRequest request) {
        // Find the most recent invitation for the email
        UserInvitation invitation = invitationRepository.findByEmailAndUsedFalse(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No active invitation found for this email"));
        
        // Check if the invitation is already expired
        if (invitation.getExpiryDate().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Invitation is still valid and does not need renewal");
        }
        
        // Generate a new token
        String newToken = generateSecureToken();
        
        // Update the invitation with new token and expiry date
        invitation.setToken(newToken);
        invitation.setExpiryDate(LocalDateTime.now().plusDays(1));
        invitationRepository.save(invitation);
        
        // Send a new invitation email
        String invitationLink = frontendUrl + "/setup-password?token=" + newToken;
        emailService.sendInvitationEmail(invitation, invitationLink);
        
        log.info("Invitation token renewed for {}", request.getEmail());
    }
    
    private String generateSecureToken() {
        // Generate a secure random token
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public List<UserInvitation> getInvitations() {
        return invitationRepository.findAllByOrderByCreatedAtDesc();
    }
}