package com.example.nagoyameshi.event;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.VerificationTokenService;

@Component
public class PasswordEventListener {
    private final VerificationTokenService verificationTokenService;    
    private final JavaMailSender javaMailSender;
    
    public PasswordEventListener(VerificationTokenService verificationTokenService, JavaMailSender mailSender) {
        this.verificationTokenService = verificationTokenService;        
        this.javaMailSender = mailSender;
    }

    @EventListener
    private void onPasswordEvent(PasswordEvent passwordEvent) {
        User user = passwordEvent.getUser();
        String token = UUID.randomUUID().toString();
        verificationTokenService.create(user, token);
        
        String recipientAddress = user.getEmail();
        String subject = "メール認証";
        // Adjust the URL for confirmation
        String confirmationUrl = passwordEvent.getRequestUrl().replace("/pwreset", "/newpw") + "?token=" + token;
        String message = "以下のリンクをクリックしてパスワード更新へお進みください。";
        
        SimpleMailMessage mailMessage = new SimpleMailMessage(); 
        mailMessage.setTo(recipientAddress);
        mailMessage.setSubject(subject);
        mailMessage.setText(message + "\n" + confirmationUrl);
        javaMailSender.send(mailMessage);
    }
}
