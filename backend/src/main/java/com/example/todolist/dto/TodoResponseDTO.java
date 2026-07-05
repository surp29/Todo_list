package com.example.todolist.dto;

import com.example.todolist.model.TodoPriority;
import com.example.todolist.model.TodoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response payload representing a Todo item returned to clients.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoResponseDTO {

    private Long id;
    private String title;
    private String description;
    private TodoStatus status;
    private TodoPriority priority;
    private LocalDate dueDate;
    private Long assigneeId;
    private String assigneeName;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
