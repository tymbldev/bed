package com.tymbl.common.service;

import java.util.UUID;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendVerificationEmail(String to, String verificationToken) throws MessagingException {
        Context context = new Context();
        context.setVariable("verificationToken", verificationToken);
        context.setVariable("verificationUrl", frontendUrl + "/verify-email?token=" + verificationToken);
        
        String emailContent = templateEngine.process("verification-email", context);
        sendEmail(to, "Email Verification", emailContent);
    }

    public void sendPasswordResetEmail(String to, String resetToken) throws MessagingException {
        Context context = new Context();
        context.setVariable("resetToken", resetToken);
        context.setVariable("resetUrl", frontendUrl + "/reset-password?token=" + resetToken);
        
        String emailContent = templateEngine.process("password-reset-email", context);
        sendEmail(to, "Password Reset Request", emailContent);
    }

    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        
        mailSender.send(message);
    }
    
    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
    
    public String generatePasswordResetToken() {
        return UUID.randomUUID().toString();
    }
} 