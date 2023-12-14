package com.github.k7.coursein.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.k7.coursein.model.WebResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@AllArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
        throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        WebResponse<String> webResponse = WebResponse.<String>builder()
            .code(HttpStatus.FORBIDDEN.value())
            .message(HttpStatus.FORBIDDEN.getReasonPhrase())
            .errors("Access denied")
            .build();

        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, webResponse);
        out.flush();
    }
}
