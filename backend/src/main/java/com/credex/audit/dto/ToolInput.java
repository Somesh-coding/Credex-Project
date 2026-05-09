package com.credex.audit.dto;

public record ToolInput(
        String tool,
        String plan,
        double monthlySpend,
        int seats
) {}
