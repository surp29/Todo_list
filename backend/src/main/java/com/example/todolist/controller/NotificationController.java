package com.example.todolist.controller;

import com.example.todolist.dto.ApiResponseDTO;
import com.example.todolist.dto.NotificationResponseDTO;
import com.example.todolist.model.User;
import com.example.todolist.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints for retrieving and acknowledging the current user's notifications.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Thông báo trong ứng dụng")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Lấy danh sách thông báo của tài khoản đang đăng nhập")
    @GetMapping
    public ApiResponseDTO<List<NotificationResponseDTO>> listNotifications(
            @AuthenticationPrincipal User currentUser) {
        return ApiResponseDTO.success("Lấy danh sách thông báo thành công",
                notificationService.listForUser(currentUser));
    }

    @Operation(summary = "Đánh dấu một thông báo đã đọc")
    @PatchMapping("/{id}/read")
    public ApiResponseDTO<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        notificationService.markAsRead(id, currentUser);
        return ApiResponseDTO.success("Đã đánh dấu đã đọc", null);
    }

    @Operation(summary = "Đánh dấu toàn bộ thông báo của tài khoản đang đăng nhập là đã đọc")
    @PatchMapping("/read-all")
    public ApiResponseDTO<Void> markAllAsRead(@AuthenticationPrincipal User currentUser) {
        notificationService.markAllAsRead(currentUser);
        return ApiResponseDTO.success("Đã đánh dấu tất cả đã đọc", null);
    }
}
