package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.ResetPassword;
import com.github.k7.coursein.entity.User;
import com.github.k7.coursein.model.ForgotPasswordRequest;
import com.github.k7.coursein.model.ForgotPasswordResponse;
import com.github.k7.coursein.model.LoginRequest;
import com.github.k7.coursein.model.SendEmailRequest;
import com.github.k7.coursein.repository.ResetPasswordRepository;
import com.github.k7.coursein.model.UserResponse;
import com.github.k7.coursein.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final ResetPasswordRepository resetPasswordRepository;

    private final JwtService jwtService;

    private final JavaMailSender javaMailSender;

    private final AuthenticationManager authenticationManager;

    private final ValidationService validationService;

    private final PasswordEncoder passwordEncoder;

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

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please verify your email first!");
        }

        return UserServiceImpl.toUserResponse(user);
    }

    public String createToken(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        return jwtService.generateToken(user);
    }

    @Override
    public void sendForgotPasswordEmail(String toEmail, String resetLink) {
        log.info("Sending resetlink to email: {}", toEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("CourseIn - Forgot Password");

        String emailText = "Klik link di bawah ini untuk melakukan konfirmasi reset password anda"
            + "\njika anda tidak merasa mengirim permintaan, silahkan abaikan email ini.\n\n"
            + resetLink
            + "\n\nCourseIn team";

        message.setText(emailText);

        javaMailSender.send(message);

        log.info("Success resetlink to email: {}", toEmail);
    }

    @Override
    public ResetPassword generateResetToken(String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiredDate = LocalDateTime.now().plusMinutes(5);

        ResetPassword resetPassword = ResetPassword.builder()
            .token(token)
            .email(email)
            .expiredDate(expiredDate)
            .build();

        return resetPasswordRepository.save(resetPassword);
    }

    @Override
    @Transactional
    public ForgotPasswordResponse requestForgotPassword(SendEmailRequest request) {
        validationService.validate(request);

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User email not found"));

        if (!user.isEnabled()) {
            log.warn("Request reset password failed. User with email: {} is disabeld", user.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is disabled, cannot request reset password");
        }

        ResetPassword resetPassword = generateResetToken(user.getEmail());
        resetPasswordRepository.save(resetPassword);

        String urlPageResetPassword = "https://fpbinar-kel7.vercel.app/resetpassword-confirm/";

        String resetLink = urlPageResetPassword + resetPassword.getToken();

        sendForgotPasswordEmail(user.getEmail(), resetLink);

        return toForgotPasswordResponse(resetPassword);
    }

    private ForgotPasswordResponse toForgotPasswordResponse(ResetPassword resetPassword) {
        return ForgotPasswordResponse.builder()
            .email(resetPassword.getEmail())
            .token(resetPassword.getToken())
            .build();
    }

    @Override
    @Transactional
    public void confirmForgotPassword(ForgotPasswordRequest request) {
        validationService.validate(request);

        ResetPassword resetPassword = resetPasswordRepository.findById(request.getToken())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid reset token"));

        if (resetPassword.getExpiredDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token has expired");
        }

        User user = userRepository.findByEmail(resetPassword.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User email not found"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please verify your email first!");
        }

        if (!Objects.equals(request.getNewPassword(), request.getConfirmNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        resetPasswordRepository.deleteByEmail(resetPassword.getEmail());
    }

}
