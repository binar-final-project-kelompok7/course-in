package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.RegisterOTP;
import com.github.k7.coursein.model.DeleteUserRequest;
import com.github.k7.coursein.model.RegisterOTPResponse;
import com.github.k7.coursein.model.RegisterUserRequest;
import com.github.k7.coursein.model.ResendOTPRequest;
import com.github.k7.coursein.model.UpdatePasswordUserRequest;
import com.github.k7.coursein.model.UpdateUserRequest;
import com.github.k7.coursein.model.UserResponse;
import com.github.k7.coursein.model.VerifyOtpRequest;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;

public interface UserService {

    RegisterOTPResponse registerUser(RegisterUserRequest request);

    UserResponse getUser(String username);

    UserResponse updateUser(String username, UpdateUserRequest request);

    void updatePassword(String username, UpdatePasswordUserRequest request);

    void deleteUser(String username, DeleteUserRequest request);

    long countUser();

    String verifyOTP(VerifyOtpRequest request);

    RegisterOTPResponse resendOtp(ResendOTPRequest request);

    void sendOtpToEmail(String toEmail, Integer OtpCode) throws MessagingException;

    RegisterOTP generateOtp(String email);

}
