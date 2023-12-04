package com.github.k7.coursein.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.k7.coursein.entity.Role;
import com.github.k7.coursein.entity.User;
import com.github.k7.coursein.enums.UserRole;
import com.github.k7.coursein.model.LoginRequest;
import com.github.k7.coursein.model.WebResponse;
import com.github.k7.coursein.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(UserRole.USER);

        User user = User.builder()
            .username("for_testing")
            .name("For Testing")
            .email("fortesting@example.com")
            .password(passwordEncoder.encode("password"))
            .roles(roles)
            .build();
        userRepository.save(user);
    }

    @Test
    void testAuthLogin_Success() throws Exception {
        LoginRequest request = LoginRequest.builder()
            .username("for_testing")
            .password("password")
            .build();

        mockMvc.perform(
            post("/api/v1/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<String>>() {
                }
            );
            String token = result.getResponse().getHeader("Authorization");

            assertEquals(200, response.getCode());
            assertEquals(HttpStatus.OK.getReasonPhrase(), response.getMessage());
            assertNotNull(token);
            assertTrue(token.startsWith("Bearer"));
        });
    }

    @Test
    void testAuthLogin_BadRequest() throws Exception {
        LoginRequest request = LoginRequest.builder()
            .username("for_testing1")
            .password("wrong_password")
            .build();

        mockMvc.perform(
            post("/api/v1/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<String>>() {
                }
            );
            String token = result.getResponse().getHeader("Authorization");

            assertEquals(400, response.getCode());
            assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.getMessage());
            assertNotNull(response.getErrors());
            System.out.println(response.getErrors());
            assertNull(token);
        });
    }

}
