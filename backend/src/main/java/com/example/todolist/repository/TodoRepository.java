package com.example.todolist.repository;

import com.example.todolist.model.Todo;
import com.example.todolist.model.TodoPriority;
import com.example.todolist.model.TodoStatus;
import com.example.todolist.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Todo} entities.
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {

    /**
     * Finds todos matching the optional keyword (searched in title or description),
     * status, priority and assignee filters, with pagination and sorting applied via {@code pageable}.
     *
     * @param keyword    a lowercase {@code %...%} LIKE pattern, or {@code null} to skip the filter
     * @param assigneeId when non-null, restricts results to todos assigned to that user
     *                   (used to scope an Employee's view to their own tasks)
     */
    @Query("SELECT t FROM Todo t WHERE " +
            "(:keyword IS NULL OR LOWER(t.title) LIKE :keyword " +
            "OR LOWER(t.description) LIKE :keyword) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)")
    Page<Todo> findWithFilters(@Param("keyword") String keyword,
                                @Param("status") TodoStatus status,
                                @Param("priority") TodoPriority priority,
                                @Param("assigneeId") Long assigneeId,
                                Pageable pageable);

    /**
     * Finds every todo assigned to the given user, used by {@code AnalyticsService}
     * to compute productivity metrics (no pagination — an employee's full task history).
     */
    List<Todo> findByAssignee(User assignee);
}
