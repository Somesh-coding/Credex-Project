package com.credex.audit.model;

public record Recommendation(
        String tool,
        String currentPlan,
        double currentSpend,
        String action,
        double monthlySavings,
        String reason
) {}
