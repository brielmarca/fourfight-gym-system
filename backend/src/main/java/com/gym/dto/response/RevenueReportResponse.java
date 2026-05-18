package com.gym.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RevenueReportResponse(
    List<MonthlyRevenue> monthlyRevenue,
    BigDecimal totalRevenue,
    BigDecimal averageMonthly
) {
    public record MonthlyRevenue(String month, BigDecimal revenue) {}
}