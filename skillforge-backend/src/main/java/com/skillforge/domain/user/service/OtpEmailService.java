package com.skillforge.domain.user.service;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.skillforge.exception.BadRequestException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Sends OTP emails asynchronously so the HTTP response is not blocked
 * by SMTP round-trip latency.
 */
@Service
public class OtpEmailService {

    private static final Logger log = LoggerFactory.getLogger(OtpEmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.mail.sender-name:SkillForge}")
    private String senderName;

    @Value("${app.otp.validity-minutes:5}")
    private int otpValidityMinutes;

    public OtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        log.info("==========================================================");
        log.info(" DEVELOPMENT OTP for {}: {}", toEmail, otp);
        log.info("==========================================================");

        if (fromAddress == null || fromAddress.isBlank()) {
            throw new BadRequestException(
                    "OTP email is not configured. Set MAIL_USERNAME and MAIL_PASSWORD before requesting OTP.");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setSubject("SkillForge OTP Verification");
            helper.setText(buildBody(otp), false);
            message.setFrom(new InternetAddress(fromAddress, senderName, StandardCharsets.UTF_8.name()));

            mailSender.send(message);
            log.info("OTP email dispatched to {}", toEmail);
        } catch (MailException | MessagingException | java.io.UnsupportedEncodingException ex) {
            log.error("Failed to send OTP email to {}: {}", toEmail, ex.getMessage());
            throw new BadRequestException(
                    "Failed to send OTP email. Check SMTP credentials (MAIL_USERNAME/MAIL_PASSWORD) and try again.");
        }
    }

    private String buildBody(String otp) {
        return """
                Hello,

                Your OTP for verification is: %s

                This OTP is valid for %d minutes.

                Do not share this with anyone.

                * SkillForge Team
                """.formatted(otp, otpValidityMinutes);
    }
}
