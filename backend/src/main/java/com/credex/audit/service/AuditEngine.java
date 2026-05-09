package com.credex.audit.service;

import com.credex.audit.dto.AuditRequest;
import com.credex.audit.dto.ToolInput;
import com.credex.audit.model.AuditResult;
import com.credex.audit.model.Recommendation;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class AuditEngine {

    private record Plan(String tool, String plan, double monthlyPrice, boolean perSeat) {}

    private static final List<Plan> PLANS = List.of(
            new Plan("Cursor", "Hobby", 0, false),
            new Plan("Cursor", "Pro", 20, false),
            new Plan("Cursor", "Business", 40, true),

            new Plan("GitHub Copilot", "Individual", 10, true),
            new Plan("GitHub Copilot", "Business", 19, true),
            new Plan("GitHub Copilot", "Enterprise", 39, true),

            new Plan("Claude", "Free", 0, false),
            new Plan("Claude", "Pro", 20, true),
            new Plan("Claude", "Max", 100, true),
            new Plan("Claude", "Team", 30, true),

            new Plan("ChatGPT", "Plus", 20, true),
            new Plan("ChatGPT", "Team", 30, true),
            new Plan("ChatGPT", "Enterprise", 60, true),

            new Plan("Anthropic API direct", "API direct", 0, false),
            new Plan("OpenAI API direct", "API direct", 0, false),
            new Plan("Gemini", "Pro", 20, true),
            new Plan("Gemini", "Ultra", 250, true),
            new Plan("Gemini", "API", 0, false),

            new Plan("Windsurf", "Free", 0, false),
            new Plan("Windsurf", "Pro", 20, false),
            new Plan("Windsurf", "Teams", 40, true),
            new Plan("Windsurf", "Enterprise", 60, true)
    );

    public AuditResult run(AuditRequest request) {
        AuditResult result = new AuditResult();
        result.id = UUID.randomUUID().toString();
        result.publicSlug = UUID.randomUUID().toString().substring(0, 8);
        result.teamSize = request.teamSize();
        result.useCase = request.useCase();
        result.createdAt = Instant.now();

        List<Recommendation> recommendations = new ArrayList<>();

        double totalSpend = 0;
        double totalSavings = 0;

        if (request.tools() == null || request.tools().isEmpty()) {
            result.totalMonthlySpend = 0;
            result.totalMonthlySavings = 0;
            result.totalAnnualSavings = 0;
            result.recommendations = recommendations;
            return result;
        }

        for (ToolInput input : request.tools()) {
            double currentSpend = Math.max(0, input.monthlySpend());
            int seats = Math.max(1, input.seats());

            totalSpend += currentSpend;

            Recommendation recommendation = evaluateTool(
                    input.tool(),
                    input.plan(),
                    currentSpend,
                    seats,
                    request.teamSize(),
                    request.useCase()
            );

            recommendations.add(recommendation);
            totalSavings += recommendation.monthlySavings();
        }

        result.totalMonthlySpend = round(totalSpend);
        result.totalMonthlySavings = round(totalSavings);
        result.totalAnnualSavings = round(totalSavings * 12);
        result.recommendations = recommendations;

        return result;
    }

    private Recommendation evaluateTool(
            String tool,
            String plan,
            double currentSpend,
            int seats,
            int teamSize,
            String useCase
    ) {
        Plan currentPlan = findPlan(tool, plan);

        if (currentPlan == null) {
            return new Recommendation(
                    tool,
                    plan,
                    currentSpend,
                    "Review billing manually",
                    0,
                    "This tool or plan is not in the pricing table yet, so the engine avoids inventing savings."
            );
        }

        double expectedSpend = expectedCost(currentPlan, seats);
        double bestSameVendorCost = bestSameVendorCost(currentPlan.tool(), seats, teamSize);
        double sameVendorSavings = Math.max(0, currentSpend - bestSameVendorCost);

        if (teamSize <= 2 && isTeamPlan(currentPlan.plan())) {
            double soloCost = cheapestSoloPlan(currentPlan.tool(), seats);
            double savings = Math.max(0, currentSpend - soloCost);

            return new Recommendation(
                    tool,
                    plan,
                    currentSpend,
                    "Downgrade to a solo/pro plan unless admin controls are required",
                    round(savings),
                    "For 1–2 users, team plans are usually unnecessary unless you need SSO, centralized billing, or admin controls."
            );
        }

        if (currentSpend > expectedSpend * 1.20 && expectedSpend > 0) {
            double savings = currentSpend - expectedSpend;

            return new Recommendation(
                    tool,
                    plan,
                    currentSpend,
                    "Review billing and move closer to listed plan pricing",
                    round(savings),
                    "Your spend is more than 20% above expected listed pricing for this plan and seat count."
            );
        }

        if (sameVendorSavings >= 25) {
            return new Recommendation(
                    tool,
                    plan,
                    currentSpend,
                    "Move to the cheaper same-vendor plan that fits your team",
                    round(sameVendorSavings),
                    "A lower plan from the same vendor appears sufficient based on team size and submitted spend."
            );
        }

        Recommendation alternative = cheaperAlternative(tool, plan, currentSpend, seats, useCase);
        if (alternative.monthlySavings() >= 25) {
            return alternative;
        }

        if (currentSpend >= 500) {
            double creditSavings = currentSpend * 0.20;

            return new Recommendation(
                    tool,
                    plan,
                    currentSpend,
                    "Explore discounted AI credits through Credex",
                    round(creditSavings),
                    "At this spend level, procurement through credits can often reduce retail AI costs without changing workflow."
            );
        }

        return new Recommendation(
                tool,
                plan,
                currentSpend,
                "Keep current plan",
                0,
                "Your submitted spend looks aligned with expected pricing and team size."
        );
    }

    private Plan findPlan(String tool, String plan) {
        return PLANS.stream()
                .filter(p -> normalize(p.tool()).equals(normalize(tool)))
                .filter(p -> normalize(p.plan()).equals(normalize(plan)))
                .findFirst()
                .orElse(null);
    }

    private double expectedCost(Plan plan, int seats) {
        if (plan.monthlyPrice() == 0) return 0;
        return plan.perSeat() ? plan.monthlyPrice() * seats : plan.monthlyPrice();
    }

    private double bestSameVendorCost(String tool, int seats, int teamSize) {
        return PLANS.stream()
                .filter(p -> normalize(p.tool()).equals(normalize(tool)))
                .filter(p -> {
                    if (teamSize <= 2) return !isTeamPlan(p.plan());
                    return true;
                })
                .mapToDouble(p -> expectedCost(p, seats))
                .filter(cost -> cost > 0)
                .min()
                .orElse(0);
    }

    private double cheapestSoloPlan(String tool, int seats) {
        return PLANS.stream()
                .filter(p -> normalize(p.tool()).equals(normalize(tool)))
                .filter(p -> !isTeamPlan(p.plan()))
                .mapToDouble(p -> expectedCost(p, seats))
                .filter(cost -> cost > 0)
                .min()
                .orElse(20 * seats);
    }

    private Recommendation cheaperAlternative(String tool, String plan, double currentSpend, int seats, String useCase) {
        String normalizedUseCase = normalize(useCase);

        if (normalizedUseCase.contains("coding")) {
            double copilotBusiness = 19 * seats;
            double windsurfPro = 20;

            double target = Math.min(copilotBusiness, windsurfPro);
            double savings = currentSpend - target;

            if (!normalize(tool).contains("github copilot") && savings >= 25) {
                return new Recommendation(
                        tool,
                        plan,
                        currentSpend,
                        "Compare against GitHub Copilot Business or Windsurf Pro",
                        round(savings),
                        "For coding-heavy teams, these tools can cover similar workflows at a lower baseline monthly cost."
                );
            }
        }

        if (normalizedUseCase.contains("writing") || normalizedUseCase.contains("research") || normalizedUseCase.contains("mixed")) {
            double chatgptPlus = 20 * seats;
            double claudePro = 20 * seats;
            double target = Math.min(chatgptPlus, claudePro);
            double savings = currentSpend - target;

            if (savings >= 25) {
                return new Recommendation(
                        tool,
                        plan,
                        currentSpend,
                        "Compare against ChatGPT Plus or Claude Pro",
                        round(savings),
                        "For general writing, research, and mixed use, individual pro plans may be enough before moving to team tiers."
                );
            }
        }

        return new Recommendation(
                tool,
                plan,
                currentSpend,
                "Keep current plan",
                0,
                "No cheaper alternative was clearly defensible from the submitted usage."
        );
    }

    private boolean isTeamPlan(String plan) {
        String p = normalize(plan);
        return p.contains("team") || p.contains("business") || p.contains("enterprise");
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase().replace("-", " ");
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}