package com.example.todolist.dto;

import com.example.todolist.model.Notification;
import com.example.todolist.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response payload representing a single in-app notification.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private Long id;
    private String message;
    private Long relatedTodoId;
    private String todoTitle;
    private String actorName;
    private NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponseDTO from(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .relatedTodoId(notification.getRelatedTodoId())
                .todoTitle(notification.getTodoTitle())
                .actorName(notification.getActor() != null ? notification.getActor().getFullName() : null)
                // Notifications persisted before `type` existed have a null column value;
                // they were all task-completed alerts back then (the only kind that existed).
                .type(notification.getType() != null ? notification.getType() : NotificationType.TASK_COMPLETED)
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
