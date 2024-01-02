package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.ResetPassword;
import com.github.k7.coursein.model.*;

import javax.mail.MessagingException;

public interface AuthService {

    UserResponse login(LoginRequest request);

    String createToken(String username);

    void sendForgotPasswordEmail(String toEmail, String resetLink);

    ResetPassword generateResetToken(String email);

    ForgotPasswordResponse requestForgotPassword(SendEmailRequest request);

    void confirmForgotPassword(ForgotPasswordRequest forgotPasswordRequest);

}
