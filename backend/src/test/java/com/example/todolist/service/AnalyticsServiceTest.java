package com.example.todolist.service;

import com.example.todolist.dto.ProductivityOverviewDTO;
import com.example.todolist.model.Role;
import com.example.todolist.model.Todo;
import com.example.todolist.model.TodoPriority;
import com.example.todolist.model.TodoStatus;
import com.example.todolist.model.User;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private User employee;

    @BeforeEach
    void setUp() {
        employee = User.builder()
                .id(2L)
                .username("nhanvien")
                .fullName("Nguyen Van A")
                .position("Developer")
                .role(Role.EMPLOYEE)
                .build();
    }

    private Todo todo(TodoPriority priority, TodoStatus status, LocalDate dueDate, LocalDateTime updatedAt) {
        return Todo.builder()
                .priority(priority)
                .status(status)
                .dueDate(dueDate)
                .updatedAt(updatedAt)
                .assignee(employee)
                .build();
    }

    @Test
    void test_getProductivityOverview_perfectRecord_scores100() {
        when(userRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(employee));
        List<Todo> todos = List.of(
                todo(TodoPriority.HIGH, TodoStatus.COMPLETED, LocalDate.now().plusDays(1), LocalDateTime.now()),
                todo(TodoPriority.LOW, TodoStatus.COMPLETED, null, LocalDateTime.now())
        );
        when(todoRepository.findByAssignee(employee)).thenReturn(todos);

        ProductivityOverviewDTO result = analyticsService.getProductivityOverview();

        assertThat(result.getEmployees()).hasSize(1);
        var stats = result.getEmployees().get(0);
        assertThat(stats.getProductivityScore()).isEqualTo(100.0);
        assertThat(stats.getCompletionRate()).isEqualTo(100.0);
        assertThat(stats.getOnTimeRate()).isEqualTo(100.0);
    }

    @Test
    void test_getProductivityOverview_lateCompletion_appliesPenalty() {
        when(userRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(employee));
        // HIGH priority (weight 3) completed one day after its due date -> earns half weight (1.5/3).
        List<Todo> todos = List.of(
                todo(TodoPriority.HIGH, TodoStatus.COMPLETED, LocalDate.now().minusDays(2),
                        LocalDateTime.now().minusDays(1))
        );
        when(todoRepository.findByAssignee(employee)).thenReturn(todos);

        ProductivityOverviewDTO result = analyticsService.getProductivityOverview();

        var stats = result.getEmployees().get(0);
        assertThat(stats.getProductivityScore()).isEqualTo(50.0);
        assertThat(stats.getOnTimeRate()).isEqualTo(0.0);
    }

    @Test
    void test_getProductivityOverview_pendingTasks_countTowardPossibleButNotEarned() {
        when(userRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(employee));
        List<Todo> todos = List.of(
                todo(TodoPriority.MEDIUM, TodoStatus.PENDING, null, LocalDateTime.now()),
                todo(TodoPriority.MEDIUM, TodoStatus.IN_PROGRESS, null, LocalDateTime.now())
        );
        when(todoRepository.findByAssignee(employee)).thenReturn(todos);

        ProductivityOverviewDTO result = analyticsService.getProductivityOverview();

        var stats = result.getEmployees().get(0);
        assertThat(stats.getProductivityScore()).isEqualTo(0.0);
        assertThat(stats.getPendingCount()).isEqualTo(1);
        assertThat(stats.getInProgressCount()).isEqualTo(1);
        assertThat(stats.getTotalAssigned()).isEqualTo(2);
    }

    @Test
    void test_getProductivityOverview_overdueUncompletedTask_countsAsOverdue() {
        when(userRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(employee));
        List<Todo> todos = List.of(
                todo(TodoPriority.LOW, TodoStatus.IN_PROGRESS, LocalDate.now().minusDays(1), LocalDateTime.now())
        );
        when(todoRepository.findByAssignee(employee)).thenReturn(todos);

        ProductivityOverviewDTO result = analyticsService.getProductivityOverview();

        assertThat(result.getEmployees().get(0).getOverdueCount()).isEqualTo(1);
    }

    @Test
    void test_getProductivityOverview_onHoldTask_excludedFromScoreAndOverdue() {
        when(userRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(employee));
        List<Todo> todos = List.of(
                // Completed on time -> should be the only task counted toward the score.
                todo(TodoPriority.HIGH, TodoStatus.COMPLETED, LocalDate.now().plusDays(1), LocalDateTime.now()),
                // On hold with a due date in the past: must NOT count as overdue or hurt the score.
                todo(TodoPriority.HIGH, TodoStatus.ON_HOLD, LocalDate.now().minusDays(5), LocalDateTime.now())
        );
        when(todoRepository.findByAssignee(employee)).thenReturn(todos);

        ProductivityOverviewDTO result = analyticsService.getProductivityOverview();

        var stats = result.getEmployees().get(0);
        assertThat(stats.getProductivityScore()).isEqualTo(100.0);
        assertThat(stats.getOnHoldCount()).isEqualTo(1);
        assertThat(stats.getOverdueCount()).isEqualTo(0);
        assertThat(stats.getTotalAssigned()).isEqualTo(1);
    }

    @Test
    void test_getProductivityOverview_onlyOnHoldTasks_scoresZeroNotError() {
        when(userRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(employee));
        List<Todo> todos = List.of(todo(TodoPriority.MEDIUM, TodoStatus.ON_HOLD, null, LocalDateTime.now()));
        when(todoRepository.findByAssignee(employee)).thenReturn(todos);

        ProductivityOverviewDTO result = analyticsService.getProductivityOverview();

        var stats = result.getEmployees().get(0);
        assertThat(stats.getProductivityScore()).isEqualTo(0.0);
        assertThat(stats.getOnHoldCount()).isEqualTo(1);
        assertThat(stats.getTotalAssigned()).isEqualTo(0);
    }

    @Test
    void test_getProductivityOverview_noEmployees_returnsEmptyOverview() {
        when(userRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of());

        ProductivityOverviewDTO result = analyticsService.getProductivityOverview();

        assertThat(result.getEmployees()).isEmpty();
        assertThat(result.getTeamAverageScore()).isEqualTo(0.0);
        assertThat(result.getTotalAssigned()).isEqualTo(0);
    }
}
