package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.User;
import com.github.k7.coursein.model.LoginRequest;
import com.github.k7.coursein.model.UserResponse;
import com.github.k7.coursein.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final ValidationService validationService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        validationService.validate(request);

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        log.info("Authentication Successful for user : {}", request.getUsername());

        User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        return UserServiceImpl.toUserResponse(user);
    }

    public String createToken(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        return jwtService.generateToken(user);
    }

}
