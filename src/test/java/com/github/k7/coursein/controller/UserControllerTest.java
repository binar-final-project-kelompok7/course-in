package com.github.k7.coursein.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.k7.coursein.entity.User;
import com.github.k7.coursein.enums.UserRole;
import com.github.k7.coursein.model.DeleteUserRequest;
import com.github.k7.coursein.model.RegisterUserRequest;
import com.github.k7.coursein.model.UpdatePasswordUserRequest;
import com.github.k7.coursein.model.UpdateUserRequest;
import com.github.k7.coursein.model.UserResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = User.builder()
            .username("TestUser")
            .name("Test User")
            .password(passwordEncoder.encode("Password"))
            .email("testuser@example.com")
            .build();

        userRepository.save(user);
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        RegisterUserRequest request = RegisterUserRequest.builder()
            .username("TestUserSuccess")
            .name("Test User Success")
            .password("Password")
            .email("testusersuccess@example.com")
            .build();

        mockMvc.perform(
            post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isCreated()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<String>>() {
                }
            );

            String token = result.getResponse().getHeader("Authorization");

            assertNotNull(token);
            assertTrue(token.startsWith("Bearer "));
            assertEquals(201, response.getCode());
            assertEquals(HttpStatus.CREATED.getReasonPhrase(), response.getMessage());
        });
    }

    @Test
    void testRegisterUser_BadRequest() throws Exception {
        RegisterUserRequest request = RegisterUserRequest.builder()
            .username("TestUserFailed")
            .name("Test User")
            .password("Password")
            .build();

        mockMvc.perform(
            post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isBadRequest()
        ).andDo(result -> {
            String token = result.getResponse().getHeader("Authorization");

            WebResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<String>>() {
                }
            );

            assertNull(token);
            assertEquals(400, response.getCode());
            assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.getMessage());
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testRegisterUser_Conflict() throws Exception {
        RegisterUserRequest request = RegisterUserRequest.builder()
            .username("TestUser")
            .name("Test User")
            .password("Password")
            .email("testuser@example.com")
            .build();

        mockMvc.perform(
            post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isConflict()
        ).andDo(result -> {
            String token = result.getResponse().getHeader("Authorization");

            WebResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<String>>() {
                }
            );

            assertNull(token);
            assertEquals(409, response.getCode());
            assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), response.getMessage());
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testGetUser_Success() throws Exception {
        String username = "TestUser";
        mockMvc.perform(
            get("/api/v1/users/{username}", username)
                .with(user(username).password("Password").roles(UserRole.USER.name()))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
            status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<UserResponse>>() {
                }
            );

            assertEquals(200, response.getCode());
            assertEquals(HttpStatus.OK.getReasonPhrase(), response.getMessage());
            assertEquals(username, response.getData().getUsername());
            assertEquals("Test User", response.getData().getName());
            assertEquals("testuser@example.com", response.getData().getEmail());
            assertNotNull(response.getData().getCreatedAt());
            assertNotNull(response.getData().getUpdatedAt());
        });
    }

    @Test
    void testGetUser_NotFound() throws Exception {
        String username = "TestUserNotFound";
        mockMvc.perform(
            get("/api/v1/users/{username}", username)
                .with(user(username).password("Password").roles(UserRole.USER.name()))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
            status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<String>>() {
                }
            );

            assertEquals(404, response.getCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response.getMessage());
            assertNull(response.getData());
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testGetUser_Unauthorized() throws Exception {
        String username = "TestUser";
        mockMvc.perform(
            get("/api/v1/users/{username}", username)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
            status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<String>>() {
                }
            );

            assertEquals(401, response.getCode());
            assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), response.getMessage());
            assertNull(response.getData());
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
            .username("TestUserUpdate")
            .name("Test Updated Name")
            .email("testuserupdate@example.com")
            .build();

        String username = "TestUser";
        mockMvc.perform(
            patch("/api/v1/users/{username}", username)
                .with(user(username).password("Password").roles(UserRole.USER.name()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<UserResponse>>() {
                }
            );

            assertEquals(200, response.getCode());
            assertEquals(HttpStatus.OK.getReasonPhrase(), response.getMessage());
            assertEquals("TestUserUpdate", response.getData().getUsername());
            assertEquals("Test Updated Name", response.getData().getName());
            assertEquals("testuserupdate@example.com", response.getData().getEmail());
        });
    }

    @Test
    void testUpdateUser_NotFound() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
            .username("TestUserUpdate")
            .name("Test Updated Name")
            .email("testuserupdate@example.com")
            .build();

        String username = "TestUserNotFound";
        mockMvc.perform(
            patch("/api/v1/users/{username}", username)
                .with(user(username).password("Password").roles(UserRole.USER.name()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isNotFound()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<UserResponse>>() {
                }
            );

            assertEquals(404, response.getCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response.getMessage());
            assertNull(response.getData());
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testUpdateUser_Unauthorized() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
            .username("TestUserUpdate")
            .name("Test Updated Name")
            .email("testuserupdate@example.com")
            .build();

        String username = "TestUser";
        mockMvc.perform(
            patch("/api/v1/users/{username}", username)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<UserResponse>>() {
                }
            );

            assertEquals(401, response.getCode());
            assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), response.getMessage());
            assertNull(response.getData());
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testUpdatePasswordUser_Success() throws Exception {
        UpdatePasswordUserRequest request = UpdatePasswordUserRequest.builder()
            .oldPassword("Password")
            .newPassword("NewPassword")
            .confirmNewPassword("NewPassword")
            .build();

        String username = "TestUser";
        mockMvc.perform(
            put("/api/v1/users/update-password/{username}", username)
                .with(user(username).password("Password").roles(UserRole.USER.name()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<UserResponse>>() {
                }
            );

            assertEquals(200, response.getCode());
            assertEquals(HttpStatus.OK.getReasonPhrase(), response.getMessage());
        });
    }

    @Test
    void testUpdatePasswordUser_NotFound() throws Exception {
        UpdatePasswordUserRequest request = UpdatePasswordUserRequest.builder()
            .oldPassword("Password")
            .newPassword("NewPassword")
            .confirmNewPassword("NewPassword")
            .build();

        String username = "TestUserNotFound";
        mockMvc.perform(
            put("/api/v1/users/update-password/{username}", username)
                .with(user(username).password("Password").roles(UserRole.USER.name()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isNotFound()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<UserResponse>>() {
                }
            );

            assertEquals(404, response.getCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response.getMessage());
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testUpdatePasswordUser_BadRequest() throws Exception {
        UpdatePasswordUserRequest request = UpdatePasswordUserRequest.builder()
            .oldPassword("Password")
            .newPassword("NewPassword")
            .confirmNewPassword("NewPasswordWrong")
            .build();

        String username = "TestUser";
        mockMvc.perform(
            put("/api/v1/users/update-password/{username}", username)
                .with(user(username).password("Password").roles(UserRole.USER.name()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isBadRequest()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<UserResponse>>() {
                }
            );

            assertEquals(400, response.getCode());
            assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.getMessage());
            assertNotNull(response.getErrors());
        });
    }


    @Test
    void testUpdatePasswordUser_Unauthorized() throws Exception {
        UpdatePasswordUserRequest request = UpdatePasswordUserRequest.builder()
            .oldPassword("Password")
            .newPassword("NewPassword")
            .confirmNewPassword("NewPassword")
            .build();

        String username = "TestUser";
        mockMvc.perform(
            put("/api/v1/users/update-password/{username}", username)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<UserResponse>>() {
                }
            );

            assertEquals(401, response.getCode());
            assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), response.getMessage());
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        DeleteUserRequest request = DeleteUserRequest.builder()
            .password("Password")
            .build();

        String username = "TestUser";
        mockMvc.perform(
            delete("/api/v1/users/delete/{username}", username)
                .with(user(username).password("Password").roles(UserRole.USER.name()))
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

            assertEquals(200, response.getCode());
            assertEquals(HttpStatus.OK.getReasonPhrase(), response.getMessage());
            assertNull(response.getData());
        });
    }

    @Test
    void testDeleteUser_NotFound() throws Exception {
        DeleteUserRequest request = DeleteUserRequest.builder()
            .password("Password")
            .build();

        String username = "TestUserNotFound";
        mockMvc.perform(
            delete("/api/v1/users/delete/{username}", username)
                .with(user(username).password("Password").roles(UserRole.USER.name()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<String>>() {
                }
            );

            assertEquals(404, response.getCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response.getMessage());
            assertNull(response.getData());
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testDeleteUser_Unauthorized() throws Exception {
        DeleteUserRequest request = DeleteUserRequest.builder()
            .password("WrongPassword")
            .build();

        String username = "TestUser";
        mockMvc.perform(
            delete("/api/v1/users/delete/{username}", username)
                .with(user(username).password("WrongPassword").roles(UserRole.USER.name()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
            status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<WebResponse<String>>() {
                }
            );

            assertEquals(401, response.getCode());
            assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), response.getMessage());
            assertNull(response.getData());
            assertNotNull(response.getErrors());
        });
    }

}
