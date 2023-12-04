package com.github.k7.coursein.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String username;

    private String email;

    private String name;

    private String createdAt;

    private String updatedAt;

}
