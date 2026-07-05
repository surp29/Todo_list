package com.example.todolist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Team-wide productivity overview: aggregate summary plus the per-employee
 * breakdown, sorted by {@code productivityScore} descending.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductivityOverviewDTO {

    private long totalAssigned;
    private long totalCompleted;
    private double teamAverageScore;
    private List<EmployeeProductivityDTO> employees;
}
