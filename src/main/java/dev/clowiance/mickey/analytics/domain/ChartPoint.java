package dev.clowiance.mickey.analytics.domain;

public record ChartPoint(String label, long value) {

    public ChartPoint {
        label = label == null || label.isBlank() ? "UNKNOWN" : label;
        value = Math.max(0, value);
    }
}
