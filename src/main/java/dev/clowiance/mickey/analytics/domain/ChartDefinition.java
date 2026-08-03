package dev.clowiance.mickey.analytics.domain;

import java.util.List;

public record ChartDefinition(String title, String subtitle, List<ChartPoint> points) {

    public ChartDefinition {
        title = title == null ? "MICKEY ANALYTICS" : title;
        subtitle = subtitle == null ? "" : subtitle;
        points = points == null || points.isEmpty()
                ? List.of(new ChartPoint("NO DATA", 0))
                : List.copyOf(points);
    }
}
