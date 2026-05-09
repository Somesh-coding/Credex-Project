package com.credex.audit.controller;

import com.credex.audit.dto.LeadRequest;
import com.credex.audit.service.EmailService;
import com.credex.audit.service.SupabaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
public class LeadController {
    private final SupabaseService supabaseService;
    private final EmailService emailService;

    public LeadController(SupabaseService supabaseService, EmailService emailService) {
        this.supabaseService = supabaseService;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<?> captureLead(@Valid @RequestBody LeadRequest request) {
        // Honeypot field. Real users never fill this hidden input.
        if (request.website() != null && !request.website().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Spam detected"));
        }

        String leadId = UUID.randomUUID().toString();

        Map<String, Object> lead = new LinkedHashMap<>();
        lead.put("id", leadId);
        lead.put("audit_id", request.auditId());
        lead.put("email", request.email());
        lead.put("company_name", request.companyName() == null ? "" : request.companyName());
        lead.put("role", request.role() == null ? "" : request.role());
        lead.put("team_size", request.teamSize());
        lead.put("created_at", Instant.now().toString());

        supabaseService.saveLead(leadId, lead);
        emailService.sendConfirmation(
                request.email(),
                request.publicSlug(),
                false
        );

        return ResponseEntity.ok(Map.of("ok", true));
    }
}
