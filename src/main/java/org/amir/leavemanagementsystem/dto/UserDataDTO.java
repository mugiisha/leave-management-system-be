package org.amir.leavemanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.amir.leavemanagementsystem.model.Role;
import org.amir.leavemanagementsystem.model.UserStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDataDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private Role role;
    private UserStatus status;
} 