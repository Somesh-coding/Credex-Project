package com.credex.audit.service;

import com.credex.audit.model.AuditResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SupabaseService {

    private final RestClient restClient;
    private final Map<String, AuditResult> memory = new ConcurrentHashMap<>();

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key:}")
    private String serviceRoleKey;

    @Value("${supabase.enabled:false}")
    private boolean supabaseEnabled;

    public SupabaseService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public void saveAudit(AuditResult result) {
        memory.put(result.publicSlug, result);

        if (!isEnabled()) return;

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", result.id);
        row.put("public_slug", result.publicSlug);
        row.put("team_size", result.teamSize);
        row.put("use_case", result.useCase);
        row.put("total_monthly_spend", result.totalMonthlySpend);
        row.put("total_monthly_savings", result.totalMonthlySavings);
        row.put("total_annual_savings", result.totalAnnualSavings);
        row.put("recommendations", result.recommendations);
        row.put("ai_summary", result.aiSummary);

        restClient.post()
                .uri(supabaseUrl + "/rest/v1/audits")
                .headers(this::supabaseHeaders)
                .body(row)
                .retrieve()
                .toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    public AuditResult getPublicAudit(String slug) {
        AuditResult cached = memory.get(slug);
        if (cached != null) return cached;

        if (!isEnabled()) return null;

        List<Map<String, Object>> rows = restClient.get()
                .uri(supabaseUrl + "/rest/v1/audits?public_slug=eq." + slug + "&select=*")
                .headers(this::supabaseHeaders)
                .retrieve()
                .body(List.class);

        if (rows == null || rows.isEmpty()) return null;

        Map<String, Object> row = rows.get(0);
        AuditResult result = new AuditResult();
        result.id = String.valueOf(row.get("id"));
        result.publicSlug = String.valueOf(row.get("public_slug"));
        result.teamSize = ((Number) row.get("team_size")).intValue();
        result.useCase = String.valueOf(row.get("use_case"));
        result.totalMonthlySpend = toDouble(row.get("total_monthly_spend"));
        result.totalMonthlySavings = toDouble(row.get("total_monthly_savings"));
        result.totalAnnualSavings = toDouble(row.get("total_annual_savings"));
        result.aiSummary = String.valueOf(row.get("ai_summary"));

        Object recommendations = row.get("recommendations");
        result.recommendations = RecommendationMapper.fromSupabaseJson(recommendations);

        return result;
    }

    public void saveLead(String leadId, Object lead) {
        if (!isEnabled()) return;

        restClient.post()
                .uri(supabaseUrl + "/rest/v1/leads")
                .headers(this::supabaseHeaders)
                .body(lead)
                .retrieve()
                .toBodilessEntity();
    }

    private boolean isEnabled() {
        return supabaseEnabled &&
                supabaseUrl != null && !supabaseUrl.isBlank() &&
                serviceRoleKey != null && !serviceRoleKey.isBlank();
    }

    private void supabaseHeaders(HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", serviceRoleKey);
        headers.setBearerAuth(serviceRoleKey);
        headers.set("Prefer", "return=minimal");
    }

    private double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(value));
    }
}
