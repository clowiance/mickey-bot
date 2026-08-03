package dev.clowiance.mickey.analytics.application;

import dev.clowiance.mickey.analytics.domain.ChartDefinition;

public interface ChartRendererPort {
    byte[] render(ChartDefinition chart);
}
