package com.github.k7.coursein.service;

import com.github.k7.coursein.model.DeleteUserRequest;
import com.github.k7.coursein.model.RegisterUserRequest;
import com.github.k7.coursein.model.UpdatePasswordUserRequest;
import com.github.k7.coursein.model.UpdateUserRequest;
import com.github.k7.coursein.model.UploadImageRequest;
import com.github.k7.coursein.model.UserResponse;

import java.io.IOException;


public interface UserService {

    String registerUser(RegisterUserRequest request);

    UserResponse getUser(String username);

    UserResponse updateUser(String username, UpdateUserRequest request);

    UserResponse updateProfilePicture(String username, UploadImageRequest request) throws IOException;

    void updatePassword(String username, UpdatePasswordUserRequest request);

    void deleteUser(String username, DeleteUserRequest request);

    long countUser();

}
