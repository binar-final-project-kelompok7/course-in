package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.ResetPassword;
import com.github.k7.coursein.model.LoginRequest;
import com.github.k7.coursein.model.ForgotPasswordRequest;
import com.github.k7.coursein.model.SendEmailRequest;

import javax.mail.MessagingException;
import com.github.k7.coursein.model.UserResponse;

public interface AuthService {

    UserResponse login(LoginRequest request);

    String createToken(String username);

    void sendForgotPasswordEmail(String toEmail, String resetLink) throws MessagingException;

    ResetPassword generateResetToken(String email);

    ResetPassword requestForgotPassword(SendEmailRequest request) throws MessagingException;

    ResetPassword confirmForgotPassword(ForgotPasswordRequest forgotPasswordRequest);

}
