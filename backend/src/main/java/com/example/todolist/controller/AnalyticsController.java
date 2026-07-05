package com.example.todolist.controller;

import com.example.todolist.dto.ApiResponseDTO;
import com.example.todolist.dto.ProductivityOverviewDTO;
import com.example.todolist.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Leader-only endpoint exposing team productivity metrics.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LEADER')")
@Tag(name = "Analytics", description = "Thống kê năng suất làm việc (chỉ Leader)")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Lấy tổng quan năng suất làm việc của cả nhóm")
    @GetMapping("/productivity")
    public ApiResponseDTO<ProductivityOverviewDTO> getProductivityOverview() {
        return ApiResponseDTO.success("Lấy dữ liệu năng suất thành công", analyticsService.getProductivityOverview());
    }
}
