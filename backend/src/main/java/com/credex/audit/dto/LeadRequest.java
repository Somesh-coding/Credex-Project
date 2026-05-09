package com.credex.audit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LeadRequest(
        @NotBlank String auditId,
        @NotBlank String publicSlug,
        @Email @NotBlank String email,
        String companyName,
        String role,
        int teamSize,
        String website
) {}