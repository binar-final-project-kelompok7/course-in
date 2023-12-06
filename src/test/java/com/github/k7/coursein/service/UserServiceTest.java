package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Role;
import com.github.k7.coursein.entity.User;
import com.github.k7.coursein.enums.UserRole;
import com.github.k7.coursein.model.DeleteUserRequest;
import com.github.k7.coursein.model.RegisterUserRequest;
import com.github.k7.coursein.model.UpdatePasswordUserRequest;
import com.github.k7.coursein.model.UpdateUserRequest;
import com.github.k7.coursein.model.UserResponse;
import com.github.k7.coursein.repository.RoleRepository;
import com.github.k7.coursein.repository.UserRepository;
import com.github.k7.coursein.util.TimeUtil;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private ValidationServiceImpl validationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testRegister_Success() {
        RegisterUserRequest request = RegisterUserRequest.builder()
            .username("TestUser")
            .email("testuser@example.com")
            .password("Password")
            .build();

        Role role = new Role();
        role.setName(UserRole.USER);

        when(userRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail()))
            .thenReturn(false);
        when(roleRepository.findByName(UserRole.USER))
            .thenReturn(role);
        when(jwtService.generateToken(any(User.class)))
            .thenReturn("token");
        when(passwordEncoder.encode(request.getPassword()))
            .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
            .thenReturn(new User());

        doNothing().when(validationService).validate(request);

        String token = userService.registerUser(request);

        assertNotNull(token);

        verify(validationService, times(1))
            .validate(request);
        verify(userRepository, times(1))
            .existsByUsernameOrEmail(request.getUsername(), request.getEmail());
        verify(passwordEncoder, times(1))
            .encode(request.getPassword());
        verify(roleRepository, times(1))
            .findByName(UserRole.USER);
        verify(userRepository, times(1))
            .save(any(User.class));
        verify(jwtService, times(1))
            .generateToken(any(User.class));
    }

    @Test
    void testRegister_Failed() {
        RegisterUserRequest request = RegisterUserRequest.builder()
            .username("TestUser")
            .email("testuser@example.com")
            .password("Password")
            .build();

        Role role = new Role();
        role.setName(UserRole.USER);

        when(userRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail()))
            .thenReturn(true);
        when(roleRepository.findByName(UserRole.USER))
            .thenReturn(role);
        when(jwtService.generateToken(any(User.class)))
            .thenReturn("token");
        when(passwordEncoder.encode(request.getPassword()))
            .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
            .thenReturn(new User());

        doNothing().when(validationService).validate(request);

        assertThrows(ResponseStatusException.class, () -> userService.registerUser(request));

        verify(validationService, times(1))
            .validate(request);
        verify(userRepository, times(1))
            .existsByUsernameOrEmail(request.getUsername(), request.getEmail());
        verify(passwordEncoder, times(0))
            .encode(request.getPassword());
        verify(roleRepository, times(0))
            .findByName(UserRole.USER);
        verify(userRepository, times(0))
            .save(any(User.class));
        verify(jwtService, times(0))
            .generateToken(any(User.class));
    }

    @Test
    void testGetUser_Success() {
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(UserRole.USER);

        roles.add(role);

        User user = User.builder()
            .id(1L)
            .username("TestUser")
            .name("Test User")
            .email("testuser@example.com")
            .password("Password")
            .createdAt(TimeUtil.getFormattedLocalDateTimeNow())
            .updatedAt(TimeUtil.getFormattedLocalDateTimeNow())
            .roles(roles)
            .build();

        when(userRepository.findByUsernameOrEmail(user.getUsername(), user.getUsername()))
            .thenReturn(Optional.of(user));

        UserResponse result = userService.getUser(user.getUsername());

        assertEquals("TestUser", result.getUsername());
        assertEquals("Test User", result.getName());
        assertEquals("testuser@example.com", result.getEmail());
        assertEquals(TimeUtil.formatToString(user.getCreatedAt()), result.getCreatedAt());
        assertEquals(TimeUtil.formatToString(user.getUpdatedAt()), result.getUpdatedAt());

        verify(userRepository, times(1))
            .findByUsernameOrEmail(user.getUsername(), user.getUsername());
    }

    @Test
    void testGetUser_Failed() {
        when(userRepository.findByUsernameOrEmail("TestUser", "TestUser"))
            .thenThrow(ResponseStatusException.class);

        assertThrows(ResponseStatusException.class, () -> userService.getUser("TestUser"));

        verify(userRepository, times(1))
            .findByUsernameOrEmail("TestUser", "TestUser");
    }

    @Test
    void testUpdateUser_Success() {
        UpdateUserRequest request = UpdateUserRequest.builder()
            .username("TestUserNew")
            .name("Test User New")
            .email("testuserupdate@example.com")
            .build();

        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(UserRole.USER);

        roles.add(role);

        User user = User.builder()
            .id(1L)
            .username("TestUser")
            .name("Test User")
            .email("testuser@example.com")
            .password("EncodedPassword")
            .createdAt(TimeUtil.getFormattedLocalDateTimeNow())
            .updatedAt(TimeUtil.getFormattedLocalDateTimeNow())
            .roles(roles)
            .build();

        User updatedUser = User.builder()
            .id(1L)
            .username(request.getUsername())
            .name(request.getName())
            .email("testuserupdate@example.com")
            .password("EncodedPassword")
            .createdAt(TimeUtil.getFormattedLocalDateTimeNow())
            .updatedAt(TimeUtil.getFormattedLocalDateTimeNow())
            .roles(roles)
            .build();

        assert user != null;

        when(userRepository.findByUsername(user.getUsername()))
            .thenReturn(Optional.of(user));
        when(userRepository.save(updatedUser))
            .thenReturn(updatedUser);

        doNothing().when(validationService).validate(request);

        UserResponse result = userService.updateUser("TestUser", request);

        assertEquals(request.getUsername(), result.getUsername());
        assertEquals(request.getName(), result.getName());
        assertEquals("testuserupdate@example.com", result.getEmail());
        assertEquals(TimeUtil.formatToString(updatedUser.getCreatedAt()), result.getCreatedAt());
        assertEquals(TimeUtil.formatToString(updatedUser.getUpdatedAt()), result.getUpdatedAt());

        verify(validationService, times(1))
            .validate(request);
        verify(userRepository, times(1))
            .findByUsername("TestUser");
        verify(userRepository, times(1))
            .save(updatedUser);
    }

    @Test
    void testUpdateUser_Failed() {
        UpdateUserRequest request = UpdateUserRequest.builder()
            .username("TestUserNew")
            .name("Test User New")
            .email("testuserupdate@example.com")
            .build();

        doNothing().when(validationService).validate(request);
        when(userRepository.findByUsername("TestUser"))
            .thenThrow(ResponseStatusException.class);

        assertThrows(ResponseStatusException.class, () -> userService.updateUser("TestUser", request));

        verify(validationService, times(1))
            .validate(request);
        verify(userRepository, times(1))
            .findByUsername("TestUser");
        verify(passwordEncoder, times(0))
            .encode(request.getEmail());
        verify(userRepository, times(0))
            .save(any(User.class));
    }

    @Test
    void testUpdatePassword_Success() {
        UpdatePasswordUserRequest request = UpdatePasswordUserRequest.builder()
            .oldPassword("Password")
            .newPassword("NewPassword")
            .confirmNewPassword("NewPassword")
            .build();

        User user = User.builder()
            .id(1L)
            .username("TestUser")
            .name("Test User")
            .email("testuser@example.com")
            .password("EncodedPassword")
            .createdAt(TimeUtil.getFormattedLocalDateTimeNow())
            .updatedAt(TimeUtil.getFormattedLocalDateTimeNow())
            .build();

        User updatedUser = User.builder()
            .id(1L)
            .username("TestUser")
            .name("Test User")
            .email("testuser@example.com")
            .password("EncodedNewPassword")
            .createdAt(TimeUtil.getFormattedLocalDateTimeNow())
            .updatedAt(TimeUtil.getFormattedLocalDateTimeNow())
            .build();

        when(userRepository.findByUsername("TestUser"))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
            .thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword()))
            .thenReturn("EncodedNewPassword");
        when(userRepository.save(user))
            .thenReturn(updatedUser);

        doNothing().when(validationService).validate(request);

        userService.updatePassword("TestUser", request);

        verify(validationService, times(1))
            .validate(request);
        verify(userRepository, times(1))
            .findByUsername("TestUser");
        verify(passwordEncoder, times(1))
            .matches(request.getOldPassword(), "EncodedPassword");
        verify(passwordEncoder, times(1))
            .encode(request.getNewPassword());
        verify(userRepository, times(1))
            .save(user);
    }

    @Test
    void testUpdatePassword_FailedNotFound() {
        UpdatePasswordUserRequest request = UpdatePasswordUserRequest.builder()
            .oldPassword("Password")
            .newPassword("NewPassword")
            .confirmNewPassword("NewPassword")
            .build();

        when(userRepository.findByUsername("TestUser"))
            .thenThrow(ResponseStatusException.class);

        doNothing().when(validationService).validate(request);

        assertThrows(ResponseStatusException.class, () -> userService.updatePassword("TestUser", request));

        verify(validationService, times(1))
            .validate(request);
        verify(userRepository, times(1))
            .findByUsername("TestUser");
        verify(passwordEncoder, times(0))
            .matches(any(String.class), any(String.class));
        verify(passwordEncoder, times(0))
            .encode(any(String.class));
        verify(userRepository, times(0))
            .save(any(User.class));
    }

    @Test
    void testUpdatePassword_FailedInvalidPassword() {
        UpdatePasswordUserRequest request = UpdatePasswordUserRequest.builder()
            .oldPassword("Password")
            .newPassword("NewPassword")
            .confirmNewPassword("NewPassword")
            .build();

        User user = User.builder()
            .id(1L)
            .username("TestUser")
            .name("Test User")
            .email("testuser@example.com")
            .password("EncodedPassword")
            .createdAt(TimeUtil.getFormattedLocalDateTimeNow())
            .updatedAt(TimeUtil.getFormattedLocalDateTimeNow())
            .build();

        when(userRepository.findByUsername("TestUser"))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
            .thenThrow(ResponseStatusException.class);

        doNothing().when(validationService).validate(request);

        assertThrows(ResponseStatusException.class, () -> userService.updatePassword("TestUser", request));

        verify(validationService, times(1))
            .validate(request);
        verify(userRepository, times(1))
            .findByUsername("TestUser");
        verify(passwordEncoder, times(1))
            .matches(request.getOldPassword(), "EncodedPassword");
        verify(passwordEncoder, times(0))
            .encode(any(String.class));
        verify(userRepository, times(0))
            .save(any(User.class));
    }

    @Test
    void testDeleteUser_FailedNewPasswordDoesntMatch() {
        UpdatePasswordUserRequest request = UpdatePasswordUserRequest.builder()
            .oldPassword("Password")
            .newPassword("NewPassword")
            .confirmNewPassword("NewPasswordWrong")
            .build();

        User user = User.builder()
            .id(1L)
            .username("TestUser")
            .name("Test User")
            .email("testuser@example.com")
            .password("EncodedPassword")
            .createdAt(TimeUtil.getFormattedLocalDateTimeNow())
            .updatedAt(TimeUtil.getFormattedLocalDateTimeNow())
            .build();

        when(userRepository.findByUsername("TestUser"))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
            .thenReturn(true);

        doNothing().when(validationService).validate(request);

        assertThrows(ResponseStatusException.class, () -> userService.updatePassword("TestUser", request));

        verify(validationService, times(1))
            .validate(request);
        verify(userRepository, times(1))
            .findByUsername("TestUser");
        verify(passwordEncoder, times(1))
            .matches(request.getOldPassword(), "EncodedPassword");
        verify(passwordEncoder, times(0))
            .encode(any(String.class));
        verify(userRepository, times(0))
            .save(any(User.class));
    }

    @Test
    void testDeleteUser_Success() {
        DeleteUserRequest request = DeleteUserRequest.builder()
            .password("EncodedPassword")
            .build();

        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(UserRole.USER);

        roles.add(role);

        User user = User.builder()
            .id(1L)
            .username("TestUser")
            .name("Test User")
            .email("testuser@example.com")
            .password("EncodedPassword")
            .createdAt(TimeUtil.getFormattedLocalDateTimeNow())
            .updatedAt(TimeUtil.getFormattedLocalDateTimeNow())
            .roles(roles)
            .build();

        assert user != null;

        when(userRepository.findByUsername(user.getUsername()))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
            .thenReturn(true);

        doNothing().when(validationService).validate(request);
        doNothing().when(userRepository).delete(user);

        assertDoesNotThrow(() -> userService.deleteUser("TestUser", request));

        verify(validationService, times(1))
            .validate(request);
        verify(userRepository, times(1))
            .findByUsername(user.getUsername());
        verify(passwordEncoder, times(1))
            .matches(request.getPassword(), user.getPassword());
        verify(userRepository, times(1))
            .delete(user);
    }

    @Test
    void testDeleteUser_FailedGetUsername() {
        DeleteUserRequest request = DeleteUserRequest.builder()
            .password("EncodedPassword")
            .build();

        when(userRepository.findByUsername("TestUser"))
            .thenThrow(ResponseStatusException.class);

        doNothing().when(validationService).validate(request);

        assertThrows(ResponseStatusException.class, () -> userService.deleteUser("TestUser", request));

        verify(validationService, times(1))
            .validate(request);
        verify(userRepository, times(1))
            .findByUsername("TestUser");
        verify(passwordEncoder, times(0))
            .matches(any(), any());
        verify(userRepository, times(0))
            .delete(any(User.class));
    }

    @Test
    void testDeleteUser_FailedWrongPassword() {
        DeleteUserRequest request = DeleteUserRequest.builder()
            .password("EncodedPassword")
            .build();

        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(UserRole.USER);

        roles.add(role);

        User user = User.builder()
            .id(1L)
            .username("TestUser")
            .name("Test User")
            .email("testuser@example.com")
            .password("EncodedPassword")
            .createdAt(TimeUtil.getFormattedLocalDateTimeNow())
            .updatedAt(TimeUtil.getFormattedLocalDateTimeNow())
            .roles(roles)
            .build();

        assert user != null;

        when(userRepository.findByUsername(user.getUsername()))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
            .thenReturn(false);

        doNothing().when(validationService).validate(request);

        assertThrows(ResponseStatusException.class, () -> userService.deleteUser("TestUser", request));

        verify(validationService, times(1))
            .validate(request);
        verify(userRepository, times(1))
            .findByUsername(user.getUsername());
        verify(passwordEncoder, times(1))
            .matches(request.getPassword(), user.getPassword());
        verify(userRepository, times(0))
            .delete(any(User.class));
    }

}
