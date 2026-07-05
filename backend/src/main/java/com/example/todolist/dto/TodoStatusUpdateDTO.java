package com.example.todolist.dto;

import com.example.todolist.model.TodoStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request payload used to update only the status of a Todo item.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoStatusUpdateDTO {

    @NotNull(message = "Trạng thái không được để trống")
    private TodoStatus status;
}
