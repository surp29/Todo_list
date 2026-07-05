package com.example.todolist.security;

import com.example.todolist.dto.ApiResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs at the security-filter level, before the request ever reaches a
 * controller, so it cannot rely on {@code GlobalExceptionHandler}. Returns
 * the same {@link ApiResponseDTO} envelope as the rest of the API instead of
 * Spring Security's default empty-body 403 for missing/invalid credentials.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws java.io.IOException {
        String message = "Yêu cầu đăng nhập để thực hiện thao tác này";
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponseDTO.error(message, List.of(message))));
    }
}
