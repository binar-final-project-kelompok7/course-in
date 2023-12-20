package com.github.k7.coursein.service;

import com.github.k7.coursein.model.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {

    String registerUser(RegisterUserRequest request);

    UserResponse getUser(String username);

    UserResponse updateUser(String username, UpdateUserRequest request);

    UserResponse userProfilePicture(String username, UploadImageRequest request);

    void deleteUser(String username, DeleteUserRequest request);

    Long numberOfUser();

}
