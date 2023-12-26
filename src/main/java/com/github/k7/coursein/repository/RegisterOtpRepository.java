package com.github.k7.coursein.repository;

import com.github.k7.coursein.entity.RegisterOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegisterOtpRepository extends JpaRepository<RegisterOTP, Integer> {

    Optional<RegisterOTP> findByEmailAndOtpCode(String email, Integer otpCode);

    Optional<RegisterOTP> findByEmail(String email);
}
