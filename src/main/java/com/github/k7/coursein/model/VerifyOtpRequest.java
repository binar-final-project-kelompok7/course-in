package com.github.k7.coursein.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyOtpRequest {

    @NotNull
    @Digits(integer = 6, fraction = 0)
    private Integer otpCode;

    @NotBlank
    @Size(max = 100)
    private String email;

}
