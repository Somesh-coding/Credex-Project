package com.credex.audit.controller;

import com.credex.audit.dto.AuditRequest;
import com.credex.audit.model.AuditResult;
import com.credex.audit.service.AuditEngine;
import com.credex.audit.service.SummaryService;
import com.credex.audit.service.SupabaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audits")
public class AuditController {
    private final AuditEngine auditEngine;
    private final SummaryService summaryService;
    private final SupabaseService supabaseService;

    public AuditController(AuditEngine auditEngine, SummaryService summaryService, SupabaseService supabaseService) {
        this.auditEngine = auditEngine;
        this.summaryService = summaryService;
        this.supabaseService = supabaseService;
    }

    @PostMapping
    public ResponseEntity<?> createAudit(@Valid @RequestBody AuditRequest request) {
        AuditResult result = auditEngine.run(request);

        try {
            result.aiSummary = summaryService.generate(result);
        } catch (Exception e) {
            System.out.println("AI failed in controller: " + e.getMessage());
            result.aiSummary = summaryService.fallback(result);
        }

        try {
            supabaseService.saveAudit(result);
        } catch (Exception e) {
            System.out.println("Supabase save failed: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/public/{slug}")
    public ResponseEntity<?> getPublicAudit(@PathVariable String slug) {
        AuditResult result = supabaseService.getPublicAudit(slug);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }
}
