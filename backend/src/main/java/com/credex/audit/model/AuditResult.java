package com.credex.audit.model;

import java.time.Instant;
import java.util.List;

public class AuditResult {
    public String id;
    public String publicSlug;
    public int teamSize;
    public String useCase;
    public double totalMonthlySpend;
    public double totalMonthlySavings;
    public double totalAnnualSavings;
    public List<Recommendation> recommendations;
    public String aiSummary;
    public Instant createdAt;

    public boolean highSavings() {
        return totalMonthlySavings > 500;
    }
}
