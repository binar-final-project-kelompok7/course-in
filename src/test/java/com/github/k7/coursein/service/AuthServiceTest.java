package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Role;
import com.github.k7.coursein.entity.User;
import com.github.k7.coursein.enums.UserRole;
import com.github.k7.coursein.model.LoginRequest;
import com.github.k7.coursein.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    ValidationServiceImpl validationService;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtService jwtService;

    @Mock
    Authentication authentication;

    @InjectMocks
    AuthServiceImpl authService;

    LoginRequest request;

    User user;

    @BeforeEach
    void setUp() {
        request = LoginRequest.builder()
            .username("TestUser")
            .password("Password")
            .build();

        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(UserRole.ROLE_USER);

        roles.add(role);

        user = User.builder()
            .id(1L)
            .username("TestUser")
            .email("testuser@example.com")
            .password("Password")
            .roles(roles)
            .build();
        when(userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())).thenReturn(
            Optional.ofNullable(user)
        );
        when(jwtService.generateToken(user)).thenReturn("token");
    }

    @Test
    void testLogin_Success() {
        when(authenticationManager.authenticate(any()))
            .thenReturn(authentication);
        String token = authService.login(request);

        assertNotNull(token);

        doNothing().when(validationService).validate(request);

        verify(userRepository, times(1))
            .findByUsernameOrEmail("TestUser", "TestUser");
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    void testLogin_Failed() {
        when(authenticationManager.authenticate(any()))
            .thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        doNothing().when(validationService).validate(request);

        verify(userRepository, times(0))
            .findByUsernameOrEmail("TestUser", "TestUser");
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(0)).generateToken(user);
    }

}
