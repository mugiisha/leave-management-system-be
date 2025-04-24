package org.amir.leavemanagementsystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.amir.leavemanagementsystem.dto.LeaveRequest;
import org.amir.leavemanagementsystem.model.LeaveType;

public class LeaveRequestValidatorImpl implements ConstraintValidator<LeaveRequestValidator, LeaveRequest> {

    @Override
    public void initialize(LeaveRequestValidator constraintAnnotation) {
    }

    @Override
    public boolean isValid(LeaveRequest leaveRequest, ConstraintValidatorContext context) {
        if (leaveRequest == null) {
            return true;
        }

        // Check if reason is required using the isReasonRequired() method
        if (leaveRequest.isReasonRequired() && 
            (leaveRequest.getReason() == null || leaveRequest.getReason().trim().isEmpty())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Reason is required for OTHER leave type")
                  .addPropertyNode("reason")
                  .addConstraintViolation();
            return false;
        }

        return true;
    }
} 