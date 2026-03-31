package com.webbandoan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsResponse {
    private String month;
    private Double totalRevenue = 0.0;
    private List<String> revenueLabels = new ArrayList<>();
    private List<Double> revenueValues = new ArrayList<>();
    private List<String> topFoodLabels = new ArrayList<>();
    private List<Long> topFoodValues = new ArrayList<>();
}