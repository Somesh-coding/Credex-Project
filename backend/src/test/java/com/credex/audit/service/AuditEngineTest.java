package com.credex.audit.service;

import com.credex.audit.dto.AuditRequest;
import com.credex.audit.dto.ToolInput;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditEngineTest {
    private final AuditEngine engine = new AuditEngine();

    @Test
    void highSpendProducesSavings() {
        var result = engine.run(new AuditRequest(10, "coding",
                List.of(new ToolInput("OpenAI API direct", "api", 1000, 1))));
        assertTrue(result.totalMonthlySavings > 0);
    }

    @Test
    void lowSpendDoesNotManufactureSavings() {
        var result = engine.run(new AuditRequest(5, "coding",
                List.of(new ToolInput("Cursor", "Pro", 20, 1))));
        assertEquals(0, result.totalMonthlySavings);
    }

    @Test
    void tinyTeamOnTeamPlanGetsDowngradeRecommendation() {
        var result = engine.run(new AuditRequest(2, "mixed",
                List.of(new ToolInput("ChatGPT", "Team", 90, 2))));
        assertTrue(result.recommendations.get(0).action().toLowerCase().contains("downgrade"));
    }

    @Test
    void annualSavingsIsMonthlyTimesTwelve() {
        var result = engine.run(new AuditRequest(10, "coding",
                List.of(new ToolInput("Claude", "Team", 1000, 10))));
        assertEquals(result.totalMonthlySavings * 12, result.totalAnnualSavings);
    }

    @Test
    void emptyToolsReturnsZeroSavings() {
        var result = engine.run(new AuditRequest(4, "research", List.of()));
        assertEquals(0, result.totalMonthlySavings);
    }
}
