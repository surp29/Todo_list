package com.example.todolist.controller;

import com.example.todolist.dto.ApiResponseDTO;
import com.example.todolist.dto.PasswordResetRequestDTO;
import com.example.todolist.dto.UserCreateRequestDTO;
import com.example.todolist.dto.UserResponseDTO;
import com.example.todolist.dto.UserUpdateRequestDTO;
import com.example.todolist.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Operation(summary = "Sửa họ tên / chức vụ của nhân viên")
    @PutMapping("/{id}")
    public ApiResponseDTO<UserResponseDTO> updateEmployee(
            @PathVariable Long id, @Valid @RequestBody UserUpdateRequestDTO requestDTO) {
        return ApiResponseDTO.success("Cập nhật thông tin thành công", userService.updateEmployeeProfile(id, requestDTO));
    }

    @Operation(summary = "Đặt lại mật khẩu cho nhân viên (khi nhân viên quên mật khẩu)")
    @PatchMapping("/{id}/password")
    public ApiResponseDTO<Void> resetPassword(
            @PathVariable Long id, @Valid @RequestBody PasswordResetRequestDTO requestDTO) {
        userService.resetPassword(id, requestDTO);
        return ApiResponseDTO.success("Đặt lại mật khẩu thành công", null);
    }

    @Operation(summary = "Xóa nhân viên (tự động vô hiệu hóa thay vì xóa hẳn nếu đã có lịch sử công việc); "
            + "?force=true xóa vĩnh viễn một tài khoản đã vô hiệu hóa, kèm toàn bộ công việc của họ")
    @DeleteMapping("/{id}")
    public ApiResponseDTO<Void> removeEmployee(
            @PathVariable Long id, @RequestParam(defaultValue = "false") boolean force) {
        String message = userService.removeEmployee(id, force);
        return ApiResponseDTO.success(message, null);
    }
}
