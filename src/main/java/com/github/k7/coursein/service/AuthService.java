package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.ResetPassword;
import com.github.k7.coursein.model.ForgotPasswordRequest;
import com.github.k7.coursein.model.LoginRequest;
import com.github.k7.coursein.model.SendEmailRequest;
import com.github.k7.coursein.model.UserResponse;

public interface AuthService {

    UserResponse login(LoginRequest request);

    String createToken(String username);

    void sendForgotPasswordEmail(String toEmail, String resetLink);

    ResetPassword generateResetToken(String email);

    ResetPassword requestForgotPassword(SendEmailRequest request);

    void confirmForgotPassword(ForgotPasswordRequest forgotPasswordRequest);

}
