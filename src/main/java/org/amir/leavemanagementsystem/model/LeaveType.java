package org.amir.leavemanagementsystem.model;

public enum LeaveType {
    PTO("Personal Time Off"),
    SICK_LEAVE("Sick Leave"),
    COMPASSIONATE_LEAVE("Compassionate Leave"),
    MATERNITY_LEAVE("Maternity Leave"),
    OTHER("Other");
    
    private final String displayName;
    
    LeaveType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 