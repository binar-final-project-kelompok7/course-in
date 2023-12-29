package com.github.k7.coursein.controller;

import com.github.k7.coursein.entity.ResetPassword;
import com.github.k7.coursein.model.LoginRequest;
import com.github.k7.coursein.model.ForgotPasswordRequest;
import com.github.k7.coursein.model.SendEmailRequest;
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

import javax.mail.MessagingException;

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
    public ResponseEntity<WebResponse<String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);

        return ResponseEntity.status(HttpStatus.OK)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .body(WebResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .build());
    }

    @PutMapping(
        path = "/forgot-password",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<ResetPassword> requestForgotPassword(@RequestBody SendEmailRequest request) throws MessagingException {
        ResetPassword resetPassword = authService.requestForgotPassword(request);

        return WebResponse.<ResetPassword>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(resetPassword)
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
