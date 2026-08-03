package dev.clowiance.mickey.analytics.infrastructure.python;

import dev.clowiance.mickey.analytics.domain.ChartDefinition;
import dev.clowiance.mickey.analytics.domain.ChartPoint;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PythonChartRendererTest {

    @Test
    void rendersAValidPngWithoutThirdPartyPythonPackages() {
        Assumptions.assumeTrue(pythonAvailable(), "Python 3 is not installed");
        var renderer = new PythonChartRenderer("python", 10);

        byte[] result = renderer.render(new ChartDefinition(
                "TEST CHART", "JAVA PLUS PYTHON", List.of(
                new ChartPoint("MEMBERS", 42),
                new ChartPoint("REPORTS", 3))));

        assertThat(result).startsWith((byte) 0x89, (byte) 'P', (byte) 'N', (byte) 'G');
        assertThat(result.length).isGreaterThan(1_000);
    }

    private boolean pythonAvailable() {
        try {
            Process process = new ProcessBuilder("python", "--version").start();
            return process.waitFor() == 0;
        } catch (IOException exception) {
            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
