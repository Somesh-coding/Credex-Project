package com.credex.audit.service;

import com.credex.audit.model.AuditResult;
import com.credex.audit.model.Recommendation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class SummaryService {

    @Value("${openrouter.api-key:}")
    private String openRouterKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generate(AuditResult result) {

        try {

            System.out.println("OpenRouter key present = "
                    + (openRouterKey != null && !openRouterKey.isBlank()));

            if (openRouterKey == null || openRouterKey.isBlank()) {
                return fallback(result);
            }

            String prompt = buildPrompt(result);

            Map<String, Object> requestBody = Map.of(
                    "model", "openrouter/free",
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    )
            );

            org.springframework.http.HttpHeaders headers =
                    new org.springframework.http.HttpHeaders();

            headers.set("Authorization", "Bearer " + openRouterKey);
            headers.set("Content-Type", "application/json");
            headers.set("HTTP-Referer", "http://localhost:3000");
            headers.set("X-Title", "Credex AI Spend Audit");

            org.springframework.http.HttpEntity<Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(requestBody, headers);

            Map response = restTemplate.postForObject(
                    "https://openrouter.ai/api/v1/chat/completions",
                    entity,
                    Map.class
            );

            System.out.println("OpenRouter response = " + response);

            return extractText(response);

        } catch (Exception e) {

            System.out.println("OpenRouter failed: " + e.getMessage());

            return fallback(result);
        }
    }

    private String buildPrompt(AuditResult result) {

        StringBuilder recs = new StringBuilder();

        for (Recommendation r : result.recommendations) {

            recs.append("- ")
                    .append(r.tool())
                    .append(": current spend $")
                    .append(r.currentSpend())
                    .append("/mo, action: ")
                    .append(r.action())
                    .append(", savings $")
                    .append(r.monthlySavings())
                    .append("/mo, reason: ")
                    .append(r.reason())
                    .append("\\n");
        }

        return """
You are writing a personalized AI spend audit summary for a startup founder or engineering manager.

Write one polished paragraph of 80-120 words.

Rules:
- Use only the numbers provided.
- Do not invent pricing or savings.
- Mention total monthly savings and annual savings.
- Mention the single biggest recommendation.
- If savings are under $100/month, say the stack appears reasonably optimized.
- If savings are over $500/month, mention that procurement/discounted credits may be worth exploring.
- Sound like a finance-aware SaaS advisor.
- Do not mention email, company name, private data, or internal implementation.

Audit data:
Team size: %d
Primary use case: %s
Total monthly spend: $%.2f
Total monthly savings: $%.2f
Total annual savings: $%.2f

Per-tool recommendations:
%s
""".formatted(
                result.teamSize,
                result.useCase,
                result.totalMonthlySpend,
                result.totalMonthlySavings,
                result.totalAnnualSavings,
                recs.toString()
        );
    }

    private String extractText(Map response) {

        try {

            List choices = (List) response.get("choices");

            Map firstChoice = (Map) choices.get(0);

            Map message = (Map) firstChoice.get("message");

            return String.valueOf(message.get("content"));

        } catch (Exception e) {

            return "AI summary unavailable.";
        }
    }

    public String fallback(AuditResult result) {

        if (result.totalMonthlySavings < 100) {

            return "Your AI stack appears reasonably optimized based on the submitted tools and pricing. We did not identify major overspending at the moment, although pricing and credits should still be reviewed periodically as your team scales.";
        }

        return "Your AI stack shows about $"
                + result.totalMonthlySavings
                + " in potential monthly savings, or $"
                + result.totalAnnualSavings
                + " per year. The strongest opportunities come from matching plans more closely to actual team usage and reducing unnecessary premium spend.";
    }
}