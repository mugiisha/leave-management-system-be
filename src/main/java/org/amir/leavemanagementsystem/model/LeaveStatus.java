package org.amir.leavemanagementsystem.model;

public enum LeaveStatus {
    PENDING,        // When leave request is initially submitted
    APPROVED,       // When manager approves the leave request
    REJECTED,       // When manager rejects the leave request
    CANCELLED       // When employee cancels their leave request
}