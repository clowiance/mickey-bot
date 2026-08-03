package dev.clowiance.mickey.analytics.infrastructure.python;

import dev.clowiance.mickey.analytics.application.ChartRendererPort;
import dev.clowiance.mickey.analytics.domain.ChartDefinition;
import dev.clowiance.mickey.analytics.domain.ChartPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class PythonChartRenderer implements ChartRendererPort {

    private final String pythonCommand;
    private final Duration timeout;

    public PythonChartRenderer(
            @Value("${analytics.python-command:python}") String pythonCommand,
            @Value("${analytics.timeout-seconds:15}") long timeoutSeconds) {
        this.pythonCommand = pythonCommand;
        this.timeout = Duration.ofSeconds(Math.max(1, timeoutSeconds));
    }

    @Override
    public byte[] render(ChartDefinition chart) {
        Path script = null;
        Path input = null;
        Path output = null;
        try {
            script = Files.createTempFile("mickey-chart-", ".py");
            input = Files.createTempFile("mickey-chart-", ".tsv");
            output = Files.createTempFile("mickey-chart-", ".png");
            try (var source = new ClassPathResource("analytics/render_chart.py").getInputStream()) {
                Files.copy(source, script, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.writeString(input, serialize(chart), StandardCharsets.UTF_8);

            Process process = new ProcessBuilder(
                    pythonCommand, script.toString(), input.toString(), output.toString())
                    .redirectErrorStream(true)
                    .start();
            boolean completed = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            String processOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!completed) {
                process.destroyForcibly();
                throw new IllegalStateException("Python chart renderer timed out after " + timeout.toSeconds() + " seconds");
            }
            if (process.exitValue() != 0) {
                throw new IllegalStateException("Python chart renderer failed: " + processOutput.trim());
            }
            byte[] png = Files.readAllBytes(output);
            if (png.length < 8 || png[0] != (byte) 0x89 || png[1] != 'P' || png[2] != 'N' || png[3] != 'G') {
                throw new IllegalStateException("Python renderer returned an invalid PNG");
            }
            return png;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot start Python chart renderer. Check PYTHON_COMMAND.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Python chart renderer was interrupted", e);
        } finally {
            deleteQuietly(script);
            deleteQuietly(input);
            deleteQuietly(output);
        }
    }

    private String serialize(ChartDefinition chart) {
        StringBuilder text = new StringBuilder();
        text.append(clean(chart.title())).append('\n');
        text.append(clean(chart.subtitle())).append('\n');
        for (ChartPoint point : chart.points()) {
            text.append(clean(point.label())).append('\t').append(point.value()).append('\n');
        }
        return text.toString();
    }

    private String clean(String value) {
        return value == null ? "" : value.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
