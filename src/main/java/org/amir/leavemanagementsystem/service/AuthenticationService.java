package org.amir.leavemanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.amir.leavemanagementsystem.dto.AuthenticationRequest;
import org.amir.leavemanagementsystem.dto.AuthenticationResponse;
import org.amir.leavemanagementsystem.dto.RegisterRequest;
import org.amir.leavemanagementsystem.dto.UserDataDTO;
import org.amir.leavemanagementsystem.exception.BadRequestException;
import org.amir.leavemanagementsystem.exception.ForbiddenException;
import org.amir.leavemanagementsystem.exception.UnauthorizedException;
import org.amir.leavemanagementsystem.model.Role;
import org.amir.leavemanagementsystem.model.User;
import org.amir.leavemanagementsystem.model.UserStatus;
import org.amir.leavemanagementsystem.repository.UserInvitationRepository;
import org.amir.leavemanagementsystem.repository.UserRepository;
import org.amir.leavemanagementsystem.security.JwtService;
import org.amir.leavemanagementsystem.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserInvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Value("${admin.email}")
    private String adminEmail;

    public AuthenticationResponse register(RegisterRequest request) {
        // Validate input
        ValidationUtils.validateEmail(request.getEmail());
        ValidationUtils.validatePassword(request.getPassword());
        ValidationUtils.validateName(request.getFirstName(), "First name");
        ValidationUtils.validateName(request.getLastName(), "Last name");
        
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }
        
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STAFF) // Default role for new users
                .status(UserStatus.PENDING) // Set status to PENDING
                .build();
        
        userRepository.save(user);
        
        // Send email to admin about new registration using template
        String adminSubject = "New User Registration";
        String adminMessage = emailTemplateService.processAdminNotificationTemplate(
            user.getFirstName(),
            user.getLastName(),
            user.getEmail()
        );
        emailService.sendHtmlEmail(adminEmail, adminSubject, adminMessage);
        
        // Create UserDataDTO
        UserDataDTO userData = UserDataDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
        
        return AuthenticationResponse.builder()
                .user(userData)
                .message("Registration successful. You will receive an email upon administration approval to be able to login.")
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Validate input
        ValidationUtils.validateEmail(request.getEmail());
        ValidationUtils.validatePassword(request.getPassword());
        
        // Check if there's an active invitation first
        boolean hasActiveInvitation = invitationRepository.existsByEmailAndUsedFalse(request.getEmail());
        if (hasActiveInvitation) {
            throw new ForbiddenException("Please complete your registration by setting up your password using the invitation link sent to your email.");
        }
        
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        // Check if user has set their password (for invited users)
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new ForbiddenException("Your account requires password setup. Please contact an administrator.");
        }
        
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
        
        // Check if user is approved
        if (user.getStatus() != UserStatus.APPROVED) {
            throw new ForbiddenException("Your account is not approved yet. Please wait for administrator approval.");
        }
        
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        
        // Create UserDataDTO
        UserDataDTO userData = UserDataDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .department(user.getDepartment().getName())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
        
        return AuthenticationResponse.builder()
                .user(userData)
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    public AuthenticationResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadRequestException("Refresh token is required");
        }
        
        try {
            // Extract username from refresh token
            String email = jwtService.extractUsername(refreshToken);
            
            // Check if token is valid
            if (email == null) {
                throw new UnauthorizedException("Invalid refresh token");
            }
            
            // Get user from database
            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
            
            // Check if token is valid for this user
            if (!jwtService.isTokenValid(refreshToken, user)) {
                throw new UnauthorizedException("Invalid refresh token");
            }
            
            // Check if user is approved
            if (user.getStatus() != UserStatus.APPROVED) {
                throw new ForbiddenException("Your account is not approved yet. Please wait for administrator approval.");
            }
            
            // Check if user has set their password (for invited users)
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                // Check if there's an active invitation
                boolean hasActiveInvitation = invitationRepository.existsByEmailAndUsedFalse(user.getEmail());
                if (hasActiveInvitation) {
                    throw new ForbiddenException("Please complete your registration by setting up your password using the invitation link sent to your email.");
                } else {
                    throw new ForbiddenException("Your account requires password setup. Please contact an administrator.");
                }
            }
            
            // Generate new tokens
            var newJwtToken = jwtService.generateToken(user);
            var newRefreshToken = jwtService.generateRefreshToken(user);
            
            // Create UserDataDTO
            UserDataDTO userData = UserDataDTO.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .status(user.getStatus())
                    .build();
            
            return AuthenticationResponse.builder()
                    .user(userData)
                    .token(newJwtToken)
                    .refreshToken(newRefreshToken)
                    .build();
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid refresh token");
        }
    }
} 