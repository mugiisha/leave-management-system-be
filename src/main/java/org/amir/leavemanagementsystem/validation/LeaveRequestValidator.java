package org.amir.leavemanagementsystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LeaveRequestValidatorImpl.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LeaveRequestValidator {
    String message() default "Invalid leave request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 