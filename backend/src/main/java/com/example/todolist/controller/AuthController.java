package com.example.todolist.controller;

import com.example.todolist.dto.ApiResponseDTO;
import com.example.todolist.dto.UserResponseDTO;
import com.example.todolist.dto.auth.LoginRequestDTO;
import com.example.todolist.dto.auth.LoginResponseDTO;
import com.example.todolist.model.User;
import com.example.todolist.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Handles authentication: login and retrieving the current user's profile.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Đăng nhập và thông tin tài khoản hiện tại")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Operation(summary = "Đăng nhập bằng username/password, trả về JWT")
    @PostMapping("/login")
    public ApiResponseDTO<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.getUsername(), requestDTO.getPassword()));

        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user, Map.of("role", user.getRole().name()));

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .user(UserResponseDTO.from(user))
                .build();

        return ApiResponseDTO.success("Đăng nhập thành công", response);
    }

    @Operation(summary = "Lấy thông tin tài khoản đang đăng nhập")
    @GetMapping("/me")
    public ApiResponseDTO<UserResponseDTO> me(@AuthenticationPrincipal User user) {
        return ApiResponseDTO.success("Lấy thông tin thành công", UserResponseDTO.from(user));
    }
}
