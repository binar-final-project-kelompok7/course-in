package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.ResetPassword;
import com.github.k7.coursein.model.LoginRequest;
import com.github.k7.coursein.model.ForgotPasswordRequest;
import com.github.k7.coursein.model.SendEmailRequest;

public interface AuthService {

    String login(LoginRequest request);

    void sendForgotPasswordEmail(String toEmail, String resetLink);

    ResetPassword generateResetToken(String email);

    ResetPassword requestForgotPassword(SendEmailRequest request);

    ResetPassword confirmForgotPassword(ForgotPasswordRequest forgotPasswordRequest);

}
