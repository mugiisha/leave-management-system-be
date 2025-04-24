package org.amir.leavemanagementsystem.service;

import org.amir.leavemanagementsystem.dto.UserApprovalRequest;
import org.amir.leavemanagementsystem.model.Role;
import org.amir.leavemanagementsystem.model.User;
import org.amir.leavemanagementsystem.model.UserStatus;

import java.util.List;

public interface UserService {
    User getUser(Long id);
    List<User> getAllUsers();
    List<User> getUsersByStatus(UserStatus status);
    List<User> getUsersByDepartment(Long departmentId);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    User approveUser(Long userId, String department, Role role);
    User rejectUser(Long id);
    User getUserByEmail(String email);
} 