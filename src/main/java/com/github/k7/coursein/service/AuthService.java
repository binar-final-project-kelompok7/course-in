package com.github.k7.coursein.service;

import com.github.k7.coursein.model.LoginRequest;

public interface AuthService {

    String login(LoginRequest request);

}
