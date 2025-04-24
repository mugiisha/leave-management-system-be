package org.amir.leavemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.amir.leavemanagementsystem.dto.UserApprovalRequest;
import org.amir.leavemanagementsystem.exception.BadRequestException;
import org.amir.leavemanagementsystem.model.Department;
import org.amir.leavemanagementsystem.model.Role;
import org.amir.leavemanagementsystem.model.User;
import org.amir.leavemanagementsystem.model.UserStatus;
import org.amir.leavemanagementsystem.repository.UserRepository;
import org.amir.leavemanagementsystem.service.DepartmentService;
import org.amir.leavemanagementsystem.service.EmailService;
import org.amir.leavemanagementsystem.service.EmailTemplateService;
import org.amir.leavemanagementsystem.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DepartmentService departmentService;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public List<User> getUsersByDepartment(Long departmentId) {
        return userRepository.findByDepartmentId(departmentId);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = getUser(id);
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User approveUser(Long userId, String department, Role role) {
        User user = getUser(userId);
        
        if (user.getStatus() == UserStatus.APPROVED) {
            throw new BadRequestException("User is already approved");
        }

        if(role == null){
            role = Role.STAFF;
        }
        
        if (department == null) {
            throw new BadRequestException("Department assignment is mandatory for user approval");
        }
        
        Department savedDept = departmentService.getDepartment(department);
        user.setDepartment(savedDept);
        user.setStatus(UserStatus.APPROVED);
        user.setRole(role);
        
        User savedUser = userRepository.save(user);
        
        // Send approval email to the user using template
        String subject = "Account Approved";
        String message = emailTemplateService.processApprovalTemplate(
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            savedDept.getName()
        );
        emailService.sendHtmlEmail(user.getEmail(), subject, message);
        
        return savedUser;
    }

    @Override
    public User rejectUser(Long id) {
        User user = getUser(id);
        UserStatus status = user.getStatus();

        user.setStatus(UserStatus.REJECTED);

        User savedUser = userRepository.save(user);
        
        // Send rejection email to the user using template
        String subject = "Account Rejected";
        String message = emailTemplateService.processRejectionTemplate(
            user.getFirstName(),
            user.getLastName(),
            user.getEmail()
        );
        emailService.sendHtmlEmail(user.getEmail(), subject, message);
        
        return savedUser;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
} 