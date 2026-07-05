package com.example.todolist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Productivity metrics for a single employee, computed by
 * {@code AnalyticsService} from their assigned tasks.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProductivityDTO {

    private Long employeeId;
    private String employeeName;
    private String position;
    private long totalAssigned;
    private long pendingCount;
    private long inProgressCount;
    private long onHoldCount;
    private long completedCount;
    private long overdueCount;
    private double completionRate;
    private double onTimeRate;
    private double productivityScore;
}
