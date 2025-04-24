package org.amir.leavemanagementsystem.model;

public enum UserStatus {
    PENDING,    // User has registered but not yet approved
    APPROVED,   // User has been approved by admin/manager
    REJECTED,
    DISABLED// User has been rejected by admin/manager
} 