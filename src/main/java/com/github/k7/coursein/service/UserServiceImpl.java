package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.RegisterOTP;
import com.github.k7.coursein.entity.Role;
import com.github.k7.coursein.entity.User;
import com.github.k7.coursein.enums.UserRole;
import com.github.k7.coursein.model.DeleteUserRequest;
import com.github.k7.coursein.model.RegisterOTPResponse;
import com.github.k7.coursein.model.RegisterUserRequest;
import com.github.k7.coursein.model.ResendOTPRequest;
import com.github.k7.coursein.model.UpdatePasswordUserRequest;
import com.github.k7.coursein.model.UpdateUserRequest;
import com.github.k7.coursein.model.UploadImageRequest;
import com.github.k7.coursein.model.UserResponse;
import com.github.k7.coursein.model.VerifyOTPResponse;
import com.github.k7.coursein.model.VerifyOtpRequest;
import com.github.k7.coursein.repository.RegisterOtpRepository;
import com.github.k7.coursein.repository.RoleRepository;
import com.github.k7.coursein.repository.UserRepository;
import com.github.k7.coursein.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final RegisterOtpRepository registerOtpRepository;

    private final ValidationService validationService;

    private final PasswordEncoder passwordEncoder;

    private final CloudinaryService cloudinaryService;

    private static final String USER_NOT_FOUND = "User not found!";

    private final JavaMailSender javaMailSender;

    @Override
    @Transactional
    public RegisterOTPResponse registerUser(RegisterUserRequest request) {
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
            .enabled(false)
            .build();

        Set<Role> roleSet = new HashSet<>();
        Role userRole = roleRepository.findByName(UserRole.USER);
        roleSet.add(userRole);
        user.setRoles(roleSet);

        userRepository.save(user);

        log.info("Register temporary user with username : {}", request.getUsername());

        RegisterOTP registerOTP = generateOtp(request.getEmail(), request.getUsername());
        registerOtpRepository.save(registerOTP);

        sendOtpToEmail(request.getEmail(), registerOTP.getOtpCode());

        return toRegisterOTPResponse(registerOTP);
    }

    private static RegisterOTPResponse toRegisterOTPResponse(RegisterOTP registerOTP) {
        return RegisterOTPResponse.builder()
            .email(registerOTP.getEmail())
            .username(registerOTP.getUsername())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        log.info("Get user success with username : {}", username);

        return toUserResponse(user);
    }

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
            .username(user.getUsername())
            .name(user.getName())
            .email(user.getEmail())
            .pictLink(user.getProfilePicture())
            .enabled(user.isEnabled())
            .createdAt(TimeUtil.formatToString(user.getCreatedAt()))
            .updatedAt(TimeUtil.formatToString(user.getUpdatedAt()))
            .build();
    }

    @Override
    @Transactional
    public UserResponse updateUser(String username, UpdateUserRequest request) {
        validationService.validate(request);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

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
    public UserResponse updateProfilePicture(String username, UploadImageRequest request) throws Exception {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        if (Objects.nonNull(user.getProfilePicture())) {
            log.info("Found previous profile picture link");
            log.info("Deleting previous profile picture link...");
            cloudinaryService.delete(username);
            log.info("Deleted previous profile picture link");
        }

        log.info("Setting user {} new profile picture...", username);

        user.setProfilePicture(cloudinaryService.upload(username, request));

        log.info("Saving to database...");

        userRepository.save(user);

        return toUserResponse(user);
    }

    @Override
    @Transactional
    public void updatePassword(String username, UpdatePasswordUserRequest request) {
        validationService.validate(request);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

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
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        userRepository.delete(user);

        log.info("Delete user success with username : {}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUser() {
        return userRepository.count();
    }

    @Override
    @Transactional
    public VerifyOTPResponse verifyOTP(VerifyOtpRequest request) {
        validationService.validate(request);

        RegisterOTP registerOTP = registerOtpRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtpCode())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP code or Email"));

        if (registerOTP.getExpiredDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP code has expired");
        }

        User temporaryUser = userRepository.findByEmail(registerOTP.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        temporaryUser.setEnabled(true);

        User user = userRepository.save(temporaryUser);

        registerOtpRepository.delete(registerOTP);

        log.info("User {} has been successfully verified and enabled", user.getUsername());
        log.info("Delete OTP for user email: {}", registerOTP.getEmail());

        return toVerifyOTPResponse(user);
    }

    private VerifyOTPResponse toVerifyOTPResponse(User user) {
        return VerifyOTPResponse.builder()
            .username(user.getUsername())
            .build();
    }

    @Override
    @Transactional
    public RegisterOTPResponse resendOtp(ResendOTPRequest request) {
        validationService.validate(request);

        RegisterOTP registerOTP = registerOtpRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email user not found"));

        RegisterOTP newUserOtp = generateOtp(registerOTP.getEmail(), registerOTP.getUsername());

        registerOtpRepository.delete(registerOTP);

        log.info("Success to delete old user otp data with username: {}", registerOTP.getUsername());

        registerOtpRepository.save(newUserOtp);

        sendOtpToEmail(newUserOtp.getEmail(), newUserOtp.getOtpCode());

        log.info("OTP code successfully updated for email: {}", newUserOtp.getEmail());

        return toRegisterOTPResponse(newUserOtp);
    }

    @Override
    public void sendOtpToEmail(String toEmail, Integer OtpCode) {
        log.info("Sending email OTP to: {}", toEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("CourseIn - Register OTP");

        String emailText = "Terima kasih telah melakukan register "
            + "\nSilahkan masukkan kode OTP anda sebagai berikut"
            + "\nOTP : " + OtpCode
            + "\nsegera masukkan kode OTP ini sebelum usang "
            + "\n\nCourseIn Team";

        message.setText(emailText);

        javaMailSender.send(message);

        log.info("Success to send OTP email to: {}", toEmail);
    }

    @Override
    public RegisterOTP generateOtp(String email, String username) {
        Integer otpCode = new Random().nextInt(900000) + 100000;

        log.info("Generate otp for email: {}", email);

        LocalDateTime expiredDate = LocalDateTime.now().plusMinutes(5);

        RegisterOTP registerOTP = RegisterOTP.builder()
            .otpCode(otpCode)
            .username(username)
            .email(email)
            .expiredDate(expiredDate)
            .build();

        return registerOtpRepository.save(registerOTP);
    }

}
