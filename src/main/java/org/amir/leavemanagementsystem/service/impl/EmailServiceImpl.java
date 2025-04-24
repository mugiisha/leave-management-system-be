package org.amir.leavemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.amir.leavemanagementsystem.model.Leave;
import org.amir.leavemanagementsystem.model.LeaveStatus;
import org.amir.leavemanagementsystem.model.User;
import org.amir.leavemanagementsystem.model.UserInvitation;
import org.amir.leavemanagementsystem.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        try {
            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
        }
    }

    @Override
    public void sendLeaveRequestNotification(User user, Leave leave) {
        String subject = "Leave Request Confirmation";
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("leave", leave);
        context.setVariable("startDate", leave.getStartDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        context.setVariable("endDate", leave.getEndDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        
        String htmlContent = templateEngine.process("leave-request-notification", context);
        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    @Override
    public void sendLeaveApprovalNotification(User user, Leave leave) {
        String subject = "Leave Request Approved";
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("leave", leave);
        context.setVariable("startDate", leave.getStartDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        context.setVariable("endDate", leave.getEndDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        
        String htmlContent = templateEngine.process("leave-approval-notification", context);
        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    @Override
    public void sendLeaveRejectionNotification(User user, Leave leave, String reason) {
        String subject = "Leave Request Rejected";
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("leave", leave);
        context.setVariable("startDate", leave.getStartDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        context.setVariable("endDate", leave.getEndDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        context.setVariable("reason", reason);
        
        String htmlContent = templateEngine.process("leave-rejection-notification", context);
        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    @Override
    public void sendLeaveRequestToManagers(User user, Leave leave, String[] managerEmails) {
        String subject = "New Leave Request for Approval";
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("leave", leave);
        context.setVariable("startDate", leave.getStartDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        context.setVariable("endDate", leave.getEndDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        
        String htmlContent = templateEngine.process("leave-request-to-managers", context);
        
        for (String email : managerEmails) {
            sendHtmlEmail(email, subject, htmlContent);
        }
    }

    @Override
    public void sendInvitationEmail(UserInvitation invitation, String invitationLink) {
        String subject = "Invitation to Join Leave Management System";
        Context context = new Context();
        context.setVariable("invitation", invitation);
        context.setVariable("invitationLink", invitationLink);
        context.setVariable("expiryDate", invitation.getExpiryDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        
        String htmlContent = templateEngine.process("invitation-email", context);
        sendHtmlEmail(invitation.getEmail(), subject, htmlContent);
    }
    
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
        }
    }
} 