package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.ResetPassword;
import com.github.k7.coursein.model.LoginRequest;
import com.github.k7.coursein.model.ForgotPasswordRequest;
import com.github.k7.coursein.model.SendEmailRequest;

import javax.mail.MessagingException;

public interface AuthService {

    String login(LoginRequest request);

    void sendForgotPasswordEmail(String toEmail, String resetLink) throws MessagingException;

    ResetPassword generateResetToken(String email);

    ResetPassword requestForgotPassword(SendEmailRequest request) throws MessagingException;

    ResetPassword confirmForgotPassword(ForgotPasswordRequest forgotPasswordRequest);

}
