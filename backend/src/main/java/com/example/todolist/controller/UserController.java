package com.example.todolist.controller;

import com.example.todolist.dto.ApiResponseDTO;
import com.example.todolist.dto.UserCreateRequestDTO;
import com.example.todolist.dto.UserResponseDTO;
import com.example.todolist.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Leader-only endpoints for managing Employee accounts.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LEADER')")
@Tag(name = "User", description = "Quản lý tài khoản nhân viên (chỉ Leader)")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Tạo tài khoản nhân viên mới")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> createEmployee(
            @Valid @RequestBody UserCreateRequestDTO requestDTO) {
        UserResponseDTO created = userService.createEmployee(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Tạo tài khoản nhân viên thành công", created));
    }

    @Operation(summary = "Lấy danh sách nhân viên")
    @GetMapping
    public ApiResponseDTO<List<UserResponseDTO>> listEmployees() {
        return ApiResponseDTO.success("Lấy danh sách nhân viên thành công", userService.listEmployees());
    }
}
