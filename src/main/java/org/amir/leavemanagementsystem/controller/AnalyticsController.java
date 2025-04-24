package org.amir.leavemanagementsystem.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.amir.leavemanagementsystem.dto.AnalyticsResponse;
import org.amir.leavemanagementsystem.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable Long userId) {
        AnalyticsResponse analyticsResponse = analyticsService.getAnalytics(userId);
        return ResponseEntity.ok(analyticsResponse);
    }
}