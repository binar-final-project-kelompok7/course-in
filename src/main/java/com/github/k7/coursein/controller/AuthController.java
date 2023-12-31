package com.github.k7.coursein.controller;

import com.github.k7.coursein.model.ForgotPasswordRequest;
import com.github.k7.coursein.model.ForgotPasswordResponse;
import com.github.k7.coursein.model.LoginRequest;
import com.github.k7.coursein.model.SendEmailRequest;
import com.github.k7.coursein.model.UserResponse;
import com.github.k7.coursein.model.WebResponse;
import com.github.k7.coursein.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping(
        path = "/login",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<UserResponse>> login(@RequestBody LoginRequest request) {
        UserResponse userResponse = authService.login(request);
        String token = authService.createToken(userResponse.getUsername());

        return ResponseEntity.status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .body(WebResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .data(userResponse)
                .build());
    }

    @PostMapping(
        path = "/forgot-password",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<ForgotPasswordResponse> requestForgotPassword(@RequestBody SendEmailRequest request) {
        ForgotPasswordResponse response = authService.requestForgotPassword(request);

        return WebResponse.<ForgotPasswordResponse>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(response)
            .build();
    }

    @PutMapping(
        path = "/confirm-forgot-password",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> confirmForgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.confirmForgotPassword(request);

        return WebResponse.<String>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .build();
    }

}
