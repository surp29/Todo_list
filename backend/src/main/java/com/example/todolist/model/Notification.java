package com.example.todolist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * An in-app notification delivered to a user (e.g. informing a Leader that
 * an Employee has completed a task assigned to them).
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Notification extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "related_todo_id")
    private Long relatedTodoId;

    @Column(name = "todo_title", length = 255)
    private String todoTitle;

    @Column(nullable = false, length = 500)
    private String message;

    // Nullable (rather than nullable = false) so that adding this column via
    // ddl-auto=update never fails on a database with pre-existing notification rows.
    // Notifications persisted before this field existed are simply treated as
    // TASK_COMPLETED on the frontend (the only kind that existed at the time).
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;
}
