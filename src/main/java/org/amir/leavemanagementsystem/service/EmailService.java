package org.amir.leavemanagementsystem.service;

import org.amir.leavemanagementsystem.model.Leave;
import org.amir.leavemanagementsystem.model.User;
import org.amir.leavemanagementsystem.model.UserInvitation;

public interface EmailService {
    /**
     * Sends a simple email to a user
     * @param to Recipient email
     * @param subject Email subject
     * @param text Email content
     */
    void sendSimpleEmail(String to, String subject, String text);
    
    /**
     * Sends an HTML email to a user
     * @param to Recipient email
     * @param subject Email subject
     * @param htmlContent HTML content of the email
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);
    
    /**
     * Sends a leave request notification to a user
     * @param user The user to notify
     * @param leave The leave request
     */
    void sendLeaveRequestNotification(User user, Leave leave);
    
    /**
     * Sends a leave approval notification to a user
     * @param user The user to notify
     * @param leave The approved leave
     */
    void sendLeaveApprovalNotification(User user, Leave leave);
    
    /**
     * Sends a leave rejection notification to a user
     * @param user The user to notify
     * @param leave The rejected leave
     * @param reason The reason for rejection
     */
    void sendLeaveRejectionNotification(User user, Leave leave, String reason);
    
    /**
     * Sends a leave request notification to managers for approval
     * @param user The user who requested the leave
     * @param leave The leave request
     * @param managerEmails List of manager emails to notify
     */
    void sendLeaveRequestToManagers(User user, Leave leave, String[] managerEmails);
    
    /**
     * Sends an invitation email to a user
     * @param invitation The invitation details
     * @param invitationLink The link to complete the invitation
     */
    void sendInvitationEmail(UserInvitation invitation, String invitationLink);
} 