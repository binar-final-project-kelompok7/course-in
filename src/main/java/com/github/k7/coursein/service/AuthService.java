package com.github.k7.coursein.service;

import com.github.k7.coursein.model.LoginRequest;
import com.github.k7.coursein.model.UserResponse;

public interface AuthService {

    UserResponse login(LoginRequest request);

    String createToken(String username);

}
