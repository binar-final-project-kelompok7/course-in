package com.github.k7.coursein.controller;

import com.github.k7.coursein.model.DeleteUserRequest;
import com.github.k7.coursein.model.RegisterUserRequest;
import com.github.k7.coursein.model.UpdatePasswordUserRequest;
import com.github.k7.coursein.model.UpdateUserRequest;
import com.github.k7.coursein.model.UserResponse;
import com.github.k7.coursein.model.WebResponse;
import com.github.k7.coursein.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping(
        path = "/register",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<String>> registerUser(@RequestBody RegisterUserRequest request) {
        String token = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .body(WebResponse.<String>builder()
                .code(HttpStatus.CREATED.value())
                .message(HttpStatus.CREATED.getReasonPhrase())
                .build());
    }

    @GetMapping(
        path = "/{username}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> getUser(@PathVariable("username") String username) {
        log.info("Request get from {}", username);
        UserResponse response = userService.getUser(username);
        return WebResponse.<UserResponse>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(response)
            .build();
    }

    @PatchMapping(
        path = "/{username}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> updateUser(@PathVariable("username") String username,
                                                @RequestBody UpdateUserRequest request) {
        UserResponse userResponse = userService.updateUser(username, request);
        return WebResponse.<UserResponse>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(userResponse)
            .build();
    }

    @PutMapping(
        path = "/update-password/{username}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> updatePassword(@PathVariable("username") String username,
                                              @RequestBody UpdatePasswordUserRequest request) {
        userService.updatePassword(username, request);
        return WebResponse.<String>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .build();
    }

    @DeleteMapping(
        path = "/delete/{username}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> deleteUser(@PathVariable("username") String username,
                                          @RequestBody DeleteUserRequest request) {
        userService.deleteUser(username, request);
        return WebResponse.<String>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .build();
    }

    @GetMapping(
        path = "/count",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Long> countUser() {
        long count = userService.countUser();
        return WebResponse.<Long>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(count)
            .build();
    }

}
