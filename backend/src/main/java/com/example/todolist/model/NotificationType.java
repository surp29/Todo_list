package com.example.todolist.model;

/**
 * Distinguishes the kind of event a {@link Notification} was raised for, so the
 * frontend can render an appropriate icon/label per notification.
 */
public enum NotificationType {
    TASK_ASSIGNED,
    TASK_COMPLETED
}
