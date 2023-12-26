package com.github.k7.coursein.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterOTPResponse {

    Integer otpCode;

    String email;

    LocalDateTime expiredDate;
}
