package com.github.k7.coursein.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.k7.coursein.model.WebResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@AllArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        WebResponse<String> webResponse = WebResponse.<String>builder()
            .code(HttpStatus.UNAUTHORIZED.value())
            .message(HttpStatus.UNAUTHORIZED.getReasonPhrase())
            .errors("Authentication failed")
            .build();

        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, webResponse);
        out.flush();
    }

}
