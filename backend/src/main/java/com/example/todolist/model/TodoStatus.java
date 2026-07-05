package com.example.todolist.model;

/**
 * Represents the workflow state of a {@link Todo} item.
 * <p>
 * {@code ON_HOLD} means the assignee has paused the task (e.g. a more urgent
 * task came up, or they're blocked) — it is excluded from productivity scoring
 * and overdue counts so pausing a task never counts against the employee.
 */
public enum TodoStatus {
    PENDING,
    IN_PROGRESS,
    ON_HOLD,
    COMPLETED
}
