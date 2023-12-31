package com.github.k7.coursein.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "reset_password")
public class ResetPassword {

    @Id
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(name = "expired_date", nullable = false)
    private LocalDateTime expiredDate;

}
