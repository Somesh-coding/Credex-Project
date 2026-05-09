package com.credex.audit.service;

import com.credex.audit.model.Recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendationMapper {

    @SuppressWarnings("unchecked")
    public static List<Recommendation> fromSupabaseJson(Object json) {
        List<Recommendation> result = new ArrayList<>();

        if (!(json instanceof List<?> list)) {
            return result;
        }

        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                result.add(new Recommendation(
                        str(map.get("tool")),
                        str(map.get("currentPlan")),
                        dbl(map.get("currentSpend")),
                        str(map.get("action")),
                        dbl(map.get("monthlySavings")),
                        str(map.get("reason"))
                ));
            }
        }

        return result;
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static double dbl(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        if (value == null) return 0;
        return Double.parseDouble(String.valueOf(value));
    }
}
