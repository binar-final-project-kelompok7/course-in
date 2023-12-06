package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Role;
import com.github.k7.coursein.entity.User;
import com.github.k7.coursein.enums.UserRole;
import com.github.k7.coursein.model.*;
import com.github.k7.coursein.repository.RoleRepository;
import com.github.k7.coursein.repository.UserRepository;
import com.github.k7.coursein.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final JwtService jwtService;

    private final ValidationService validationService;

    private final PasswordEncoder passwordEncoder;

    private static final String USER_NOT_FOUND = "User not found!";

    @Override
    @Transactional
    public String registerUser(RegisterUserRequest request) {
        validationService.validate(request);

        if (userRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            log.warn(
                "User with username : {} or email : {} already exists", request.getUsername(), request.getEmail()
            );
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists!");
        }

        Optional<String> name = Optional.ofNullable(request.getName());

        User user = User.builder()
            .username(request.getUsername())
            .name(name.orElse(null))
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

        Set<Role> roleSet = new HashSet<>();
        Role userRole = roleRepository.findByName(UserRole.USER);
        roleSet.add(userRole);
        user.setRoles(roleSet);

        userRepository.save(user);

        log.info("Register success with username : {}", request.getUsername());

        return jwtService.generateToken(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        log.info("Get user success with username : {}", username);

        return toUserResponse(user);
    }

    private static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
            .username(user.getUsername())
            .name(user.getName())
            .email(user.getEmail())
            .createdAt(TimeUtil.formatToString(user.getCreatedAt()))
            .updatedAt(TimeUtil.formatToString(user.getUpdatedAt()))
            .build();
    }

    @Override
    @Transactional
    public UserResponse updateUser(String username, UpdateUserRequest request) {
        validationService.validate(request);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        if (Objects.nonNull(request.getUsername())) {
            user.setUsername(request.getUsername());
        }

        if (Objects.nonNull(request.getName())) {
            user.setName(request.getName());
        }

        if (Objects.nonNull(request.getEmail())) {
            if (userRepository.existsByUsernameOrEmail(null, request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists!");
            }

            user.setEmail(request.getEmail());
        }

        userRepository.save(user);

        log.info("Update user success with username : {}", username);

        return toUserResponse(user);
    }

    @Override
    @Transactional
    public void updatePassword(String username, UpdatePasswordUserRequest request) {
        validationService.validate(request);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        if (!Objects.equals(request.getNewPassword(), request.getConfirmNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be same as old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(String username, DeleteUserRequest request) {
        validationService.validate(request);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        userRepository.delete(user);

        log.info("Delete user success with username : {}", username);
    }

}
