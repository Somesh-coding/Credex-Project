package com.credex.audit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendConfirmation(String email, String publicSlug, boolean highSavings) {
        try {
            if (resendApiKey == null || resendApiKey.isBlank()) {
                System.out.println("Resend key missing. Email skipped.");
                return;
            }

            String reportUrl = frontendUrl + "/audit/" + publicSlug;

            String subject = highSavings
                    ? "Your AI spend audit found a large savings opportunity"
                    : "Your AI spend audit is ready";

            String html = """
                    <div style="font-family:Arial,sans-serif;line-height:1.6;color:#111">
                        <h2>Your AI Spend Audit is Ready</h2>
                        <p>Thanks for running your AI spend audit.</p>
                        <p>You can view your shareable report here:</p>
                        <p>
                            <a href="%s" style="background:#111;color:white;padding:12px 18px;border-radius:8px;text-decoration:none;">
                                View Audit Report
                            </a>
                        </p>
                        %s
                        <p style="font-size:12px;color:#666;margin-top:24px;">
                            This email was sent because you requested your audit report.
                        </p>
                    </div>
                    """.formatted(
                    reportUrl,
                    highSavings
                            ? "<p>Your audit shows a high savings opportunity. Credex may be able to help reduce AI infrastructure spend further through discounted credits.</p>"
                            : "<p>Your stack looks reasonably optimized. We’ll notify you when new AI pricing or credit opportunities apply.</p>"
            );

            Map<String, Object> body = Map.of(
                    "from", "Credex Audit <onboarding@resend.dev>",
                    "to", new String[]{email},
                    "subject", subject,
                    "html", html
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.postForObject(
                    "https://api.resend.com/emails",
                    entity,
                    Map.class
            );

            System.out.println("Email sent to " + email);

        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }
}