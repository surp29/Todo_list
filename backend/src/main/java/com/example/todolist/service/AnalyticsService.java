package com.example.todolist.service;

import com.example.todolist.dto.ProductivityOverviewDTO;

/**
 * Computes team productivity metrics from completed/assigned tasks.
 */
public interface AnalyticsService {

    /**
     * Builds a team-wide productivity overview: per-employee scores plus the
     * team summary, sorted by productivity score descending.
     *
     * @return the productivity overview
     */
    ProductivityOverviewDTO getProductivityOverview();
}
