package com.example.todolist.dto;

import com.example.todolist.model.TodoPriority;
import com.example.todolist.model.TodoStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request payload for creating or updating a Todo item.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoRequestDTO {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @NotNull(message = "Trạng thái không được để trống")
    private TodoStatus status;

    @NotNull(message = "Độ ưu tiên không được để trống")
    private TodoPriority priority;

    @FutureOrPresent(message = "Ngày hết hạn không được là ngày quá khứ")
    private LocalDate dueDate;

    @NotNull(message = "Phải chọn người được giao việc")
    private Long assigneeId;
}
