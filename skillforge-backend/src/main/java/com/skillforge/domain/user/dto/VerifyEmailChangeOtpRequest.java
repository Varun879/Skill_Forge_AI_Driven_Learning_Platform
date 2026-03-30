package com.skillforge.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyEmailChangeOtpRequest {

    @NotBlank(message = "New email is required")
    @Email(message = "Invalid email address")
    private String newEmail;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be exactly 6 digits")
    private String otp;

    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String v) { this.newEmail = v; }

    public String getOtp() { return otp; }
    public void setOtp(String v) { this.otp = v; }
}
