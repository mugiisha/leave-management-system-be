package org.amir.leavemanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {
    private final TemplateEngine templateEngine;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    public String processAdminNotificationTemplate(String firstName, String lastName, String email) {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("email", email);
        context.setVariable("registrationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        context.setVariable("approvalLink", frontendUrl + "/admin/users/pending");
        
        return templateEngine.process("admin-notification", context);
    }
    
    public String processApprovalTemplate(String firstName, String lastName, String email, String departmentName) {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("email", email);
        context.setVariable("departmentName", departmentName);
        
        return templateEngine.process("approval", context);
    }
    
    public String processRejectionTemplate(String firstName, String lastName, String email) {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("email", email);
        
        return templateEngine.process("rejection", context);
    }
} 