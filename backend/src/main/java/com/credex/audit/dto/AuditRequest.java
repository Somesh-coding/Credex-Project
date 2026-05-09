package com.credex.audit.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AuditRequest(
        @Min(1) int teamSize,
        @NotBlank String useCase,
        List<ToolInput> tools
) {}
