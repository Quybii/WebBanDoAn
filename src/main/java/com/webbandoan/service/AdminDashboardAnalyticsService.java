package com.webbandoan.service;

import com.webbandoan.dto.DashboardAnalyticsResponse;
import com.webbandoan.repository.FoodRepository;
import com.webbandoan.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardAnalyticsService {

    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");

    private final OrderRepository orderRepository;
    private final FoodRepository foodRepository;

    public AdminDashboardAnalyticsService(OrderRepository orderRepository, FoodRepository foodRepository) {
        this.orderRepository = orderRepository;
        this.foodRepository = foodRepository;
    }

    public DashboardAnalyticsResponse getAnalytics(YearMonth yearMonth) {
        YearMonth reportMonth = yearMonth != null ? yearMonth : YearMonth.now();
        LocalDateTime startDate = reportMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = reportMonth.plusMonths(1).atDay(1).atStartOfDay();

        DashboardAnalyticsResponse response = new DashboardAnalyticsResponse();
        response.setMonth(reportMonth.toString());

        Map<LocalDate, Double> revenueByDay = new LinkedHashMap<>();
        for (int day = 1; day <= reportMonth.lengthOfMonth(); day++) {
            LocalDate currentDay = reportMonth.atDay(day);
            revenueByDay.put(currentDay, 0.0);
        }

        List<Object[]> dailyRevenueRows = orderRepository.findDailyRevenueBetween(startDate, endDate);
        double totalRevenue = 0.0;
        for (Object[] row : dailyRevenueRows) {
            LocalDate reportDate = toLocalDate(row[0]);
            Double revenue = toDouble(row[1]);
            if (reportDate != null) {
                revenueByDay.put(reportDate, revenue != null ? revenue : 0.0);
                totalRevenue += revenue != null ? revenue : 0.0;
            }
        }

        for (Map.Entry<LocalDate, Double> entry : revenueByDay.entrySet()) {
            response.getRevenueLabels().add(entry.getKey().format(LABEL_FORMATTER));
            response.getRevenueValues().add(entry.getValue());
        }
        response.setTotalRevenue(totalRevenue);

        List<Object[]> topFoods = foodRepository.findTop5BestSellingFoodsBetween(startDate, endDate);
        for (Object[] row : topFoods) {
            response.getTopFoodLabels().add(row[1] != null ? row[1].toString() : "Không xác định");
            response.getTopFoodValues().add(toLong(row[2]));
        }

        return response;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        return LocalDate.parse(value.toString());
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.doubleValue();
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }
}