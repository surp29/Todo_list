package com.example.todolist.controller;

import com.example.todolist.dto.ApiResponseDTO;
import com.example.todolist.dto.PageResponseDTO;
import com.example.todolist.dto.TodoRequestDTO;
import com.example.todolist.dto.TodoResponseDTO;
import com.example.todolist.dto.TodoStatusUpdateDTO;
import com.example.todolist.model.TodoPriority;
import com.example.todolist.model.TodoStatus;
import com.example.todolist.model.User;
import com.example.todolist.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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

/**
 * REST controller exposing CRUD endpoints for managing Todo items.
 * Delegates all business logic to {@link TodoService}. Creating, editing and
 * deleting a task is restricted to Leaders; Employees may only view and
 * update the status of tasks assigned to them (enforced in the service layer).
 */
@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Todo", description = "Quản lý danh sách công việc (Todo)")
public class TodoController {

    private final TodoService todoService;

    @Operation(summary = "Lấy danh sách công việc có phân trang, lọc và tìm kiếm")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<TodoResponseDTO>>> getAllTodos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TodoStatus status,
            @RequestParam(required = false) TodoPriority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal User currentUser) {

        PageResponseDTO<TodoResponseDTO> result = todoService.getAllTodos(
                keyword, status, priority, assigneeId, page, size, sortBy, sortDir, currentUser);
        return ResponseEntity.ok(ApiResponseDTO.success("Lấy danh sách thành công", result));
    }

    @Operation(summary = "Lấy chi tiết một công việc theo id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TodoResponseDTO>> getTodoById(
            @PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        TodoResponseDTO todo = todoService.getTodoById(id, currentUser);
        return ResponseEntity.ok(ApiResponseDTO.success("Lấy chi tiết thành công", todo));
    }

    @Operation(summary = "Tạo mới và giao một công việc cho nhân viên (chỉ Leader)")
    @PreAuthorize("hasRole('LEADER')")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<TodoResponseDTO>> createTodo(
            @Valid @RequestBody TodoRequestDTO requestDTO, @AuthenticationPrincipal User currentUser) {
        TodoResponseDTO created = todoService.createTodo(requestDTO, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Tạo công việc thành công", created));
    }

    @Operation(summary = "Cập nhật toàn bộ thông tin một công việc (chỉ Leader)")
    @PreAuthorize("hasRole('LEADER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TodoResponseDTO>> updateTodo(
            @PathVariable Long id, @Valid @RequestBody TodoRequestDTO requestDTO,
            @AuthenticationPrincipal User currentUser) {
        TodoResponseDTO updated = todoService.updateTodo(id, requestDTO, currentUser);
        return ResponseEntity.ok(ApiResponseDTO.success("Cập nhật thành công", updated));
    }

    @Operation(summary = "Cập nhật trạng thái của một công việc")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponseDTO<TodoResponseDTO>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody TodoStatusUpdateDTO statusUpdateDTO,
            @AuthenticationPrincipal User currentUser) {
        TodoResponseDTO updated = todoService.updateStatus(id, statusUpdateDTO.getStatus(), currentUser);
        return ResponseEntity.ok(ApiResponseDTO.success("Cập nhật trạng thái thành công", updated));
    }

    @Operation(summary = "Xóa một công việc (chỉ Leader)")
    @PreAuthorize("hasRole('LEADER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        todoService.deleteTodo(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
