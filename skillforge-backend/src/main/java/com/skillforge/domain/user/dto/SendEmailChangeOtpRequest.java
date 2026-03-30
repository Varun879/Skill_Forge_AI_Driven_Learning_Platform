package com.skillforge.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SendEmailChangeOtpRequest {

    @NotBlank(message = "New email is required")
    @Email(message = "Invalid email address")
    private String newEmail;

    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String v) { this.newEmail = v; }
}
