package org.amir.leavemanagementsystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.amir.leavemanagementsystem.model.Department;
import org.amir.leavemanagementsystem.model.Role;
import org.amir.leavemanagementsystem.model.User;
import org.amir.leavemanagementsystem.model.UserStatus;
import org.amir.leavemanagementsystem.repository.UserRepository;
import org.amir.leavemanagementsystem.service.DepartmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    @Value("${admin.email}")
    private String adminEmail;
    
    @Value("${admin.password}")
    private String adminPassword;

    private final DepartmentService departmentService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Ensure Administration department exists
        departmentService.ensureAdministrationDepartmentExists();
        
        // Create default admin user if not exists
        createDefaultAdminUser();

    }
    
    private void createDefaultAdminUser() {
        if (!userRepository.existsByEmail(adminEmail)) {
            Department adminDepartment = departmentService.getDepartmentByName("Administration");
            
            User adminUser = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .status(UserStatus.APPROVED)
                    .department(adminDepartment)
                    .build();
            
            userRepository.save(adminUser);
            log.info("Default admin user created");
        }
    }
    
}