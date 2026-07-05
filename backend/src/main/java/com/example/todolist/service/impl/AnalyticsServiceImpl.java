package com.example.todolist.service.impl;

import com.example.todolist.dto.EmployeeProductivityDTO;
import com.example.todolist.dto.ProductivityOverviewDTO;
import com.example.todolist.model.Role;
import com.example.todolist.model.Todo;
import com.example.todolist.model.TodoPriority;
import com.example.todolist.model.TodoStatus;
import com.example.todolist.model.User;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Default implementation of {@link AnalyticsService}.
 * <p>
 * <b>Productivity score formula.</b> Each task contributes points equal to its
 * priority weight (LOW=1, MEDIUM=2, HIGH=3) to a "possible points" pool. A
 * completed task earns its full weight if finished on or before its due date
 * (or has no due date); a task completed <i>after</i> its due date earns only
 * half its weight (a lateness penalty). An unfinished task earns nothing yet.
 * The score is {@code earnedPoints / possiblePoints * 100}, so it rewards
 * finishing more work, finishing higher-priority work, and finishing on time —
 * all in one 0-100 number that is easy to rank employees by.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final double LATE_COMPLETION_FACTOR = 0.5;

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductivityOverviewDTO getProductivityOverview() {
        List<User> employees = userRepository.findByRole(Role.EMPLOYEE);

        List<EmployeeProductivityDTO> employeeScores = employees.stream()
                .map(employee -> computeProductivity(employee, todoRepository.findByAssignee(employee)))
                .sorted(Comparator.comparingDouble(EmployeeProductivityDTO::getProductivityScore).reversed())
                .toList();

        long totalAssigned = employeeScores.stream().mapToLong(EmployeeProductivityDTO::getTotalAssigned).sum();
        long totalCompleted = employeeScores.stream().mapToLong(EmployeeProductivityDTO::getCompletedCount).sum();
        double teamAverageScore = employeeScores.isEmpty()
                ? 0
                : employeeScores.stream().mapToDouble(EmployeeProductivityDTO::getProductivityScore).average().orElse(0);

        return ProductivityOverviewDTO.builder()
                .totalAssigned(totalAssigned)
                .totalCompleted(totalCompleted)
                .teamAverageScore(round1(teamAverageScore))
                .employees(employeeScores)
                .build();
    }

    private EmployeeProductivityDTO computeProductivity(User employee, List<Todo> todos) {
        long pending = 0;
        long inProgress = 0;
        long completed = 0;
        long overdue = 0;
        long onTimeCompleted = 0;
        double possiblePoints = 0;
        double earnedPoints = 0;
        LocalDate today = LocalDate.now();

        for (Todo todo : todos) {
            double weight = priorityWeight(todo.getPriority());
            possiblePoints += weight;

            switch (todo.getStatus()) {
                case PENDING -> pending++;
                case IN_PROGRESS -> inProgress++;
                case COMPLETED -> {
                    completed++;
                    boolean onTime = todo.getDueDate() == null
                            || !todo.getUpdatedAt().toLocalDate().isAfter(todo.getDueDate());
                    if (onTime) {
                        earnedPoints += weight;
                        onTimeCompleted++;
                    } else {
                        earnedPoints += weight * LATE_COMPLETION_FACTOR;
                    }
                }
            }

            if (todo.getStatus() != TodoStatus.COMPLETED
                    && todo.getDueDate() != null
                    && todo.getDueDate().isBefore(today)) {
                overdue++;
            }
        }

        long totalAssigned = todos.size();
        double completionRate = totalAssigned == 0 ? 0 : (completed * 100.0 / totalAssigned);
        double onTimeRate = completed == 0 ? 0 : (onTimeCompleted * 100.0 / completed);
        double productivityScore = possiblePoints == 0 ? 0 : (earnedPoints / possiblePoints * 100.0);

        return EmployeeProductivityDTO.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .position(employee.getPosition())
                .totalAssigned(totalAssigned)
                .pendingCount(pending)
                .inProgressCount(inProgress)
                .completedCount(completed)
                .overdueCount(overdue)
                .completionRate(round1(completionRate))
                .onTimeRate(round1(onTimeRate))
                .productivityScore(round1(productivityScore))
                .build();
    }

    private double priorityWeight(TodoPriority priority) {
        return switch (priority) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
