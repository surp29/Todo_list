package com.example.todolist.service;

import com.example.todolist.dto.NotificationResponseDTO;
import com.example.todolist.model.Todo;
import com.example.todolist.model.User;

import java.util.List;

/**
 * Handles creation, delivery and querying of in-app notifications.
 */
public interface NotificationService {

    /**
     * Notifies the Leader who created {@code todo} that its assignee has marked it as completed.
     * Persists the notification and pushes it in real time over WebSocket.
     *
     * @param todo the completed todo
     */
    void notifyTaskCompleted(Todo todo);

    /**
     * Lists notifications for the given recipient, most recent first.
     *
     * @param recipient the user the notifications belong to
     * @return the recipient's notifications
     */
    List<NotificationResponseDTO> listForUser(User recipient);

    /**
     * Marks a single notification as read. Only the recipient may mark their own notification.
     *
     * @param notificationId the notification identifier
     * @param recipient      the user attempting to mark it read
     */
    void markAsRead(Long notificationId, User recipient);
}
