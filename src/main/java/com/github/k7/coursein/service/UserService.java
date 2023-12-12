package com.github.k7.coursein.service;

import com.github.k7.coursein.model.DeleteUserRequest;
import com.github.k7.coursein.model.RegisterUserRequest;
import com.github.k7.coursein.model.UpdateUserRequest;
import com.github.k7.coursein.model.UserResponse;

public interface UserService {

    String registerUser(RegisterUserRequest request);

    UserResponse getUser(String username);

    UserResponse updateUser(String username, UpdateUserRequest request);

    void deleteUser(String username, DeleteUserRequest request);

    Long numberOfUser();

}
