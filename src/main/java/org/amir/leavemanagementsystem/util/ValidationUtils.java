package org.amir.leavemanagementsystem.util;

import org.amir.leavemanagementsystem.exception.BadRequestException;

public class ValidationUtils {
    
    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }
        
        // Simple email validation regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            throw new BadRequestException("Invalid email format");
        }
    }
    
    public static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }
        
        if (password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }
    }
    
    public static void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException(fieldName + " cannot be empty");
        }
    }
    
    public static void validateNotNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new BadRequestException(fieldName + " cannot be null");
        }
    }
} 