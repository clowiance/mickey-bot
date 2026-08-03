package dev.clowiance.mickey.analytics.presentation;

import dev.clowiance.mickey.analytics.application.AnalyticsDashboardService;
import dev.clowiance.mickey.analytics.domain.RenderedChart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@Service
public class AnalyticsTelegramService {

    private static final long CACHE_TTL_NANOS = Duration.ofMinutes(1).toNanos();
    private static final int MAX_CACHE_ENTRIES = 100;

    private final AnalyticsDashboardService dashboardService;
    private final Executor analyticsExecutor;
    private final Map<String, CachedChart> cache = new ConcurrentHashMap<>();

    public AnalyticsTelegramService(AnalyticsDashboardService dashboardService,
                                    @Qualifier("analyticsExecutor") Executor analyticsExecutor) {
        this.dashboardService = dashboardService;
        this.analyticsExecutor = analyticsExecutor;
    }

    public void send(TelegramLongPollingBot bot, Long chatId, String dashboard) {
        try {
            analyticsExecutor.execute(() -> renderAndSend(bot, chatId, dashboard));
        } catch (RejectedExecutionException exception) {
            log.warn("Черга аналітики переповнена для чату {}", chatId);
            sendError(bot, chatId, "⏳ Черга діаграм зайнята. Спробуйте ще раз за хвилину.");
        }
    }

    private void renderAndSend(TelegramLongPollingBot bot, Long chatId, String dashboard) {
        long startedAt = System.nanoTime();
        try {
            String cacheKey = dashboard + ":" + ("global".equals(dashboard) || "rating".equals(dashboard)
                    || "earned".equals(dashboard) ? "global" : chatId);
            RenderedChart chart = cached(cacheKey);
            if (chart == null) {
                chart = switch (dashboard) {
                    case "activity" -> dashboardService.activityOverview(chatId);
                    case "rating" -> dashboardService.economyRating(false);
                    case "earned" -> dashboardService.economyRating(true);
                    case "global" -> dashboardService.globalOverview();
                    default -> dashboardService.groupOverview(chatId);
                };
                putCache(cacheKey, chart);
            }
            SendPhoto request = new SendPhoto();
            request.setChatId(chatId);
            request.setParseMode("HTML");
            request.setCaption(chart.caption());
            request.setPhoto(new InputFile(new ByteArrayInputStream(chart.png()), chart.fileName()));
            bot.execute(request);
            log.info("Діаграму {} для чату {} надіслано за {} мс", dashboard, chatId,
                    Duration.ofNanos(System.nanoTime() - startedAt).toMillis());
        } catch (Exception exception) {
            log.error("Не вдалося побудувати діаграму {} для чату {}", dashboard, chatId, exception);
            sendError(bot, chatId, "⚠️ Не вдалося побудувати діаграму. Перевір PYTHON_COMMAND і наявність Python 3.");
        }
    }

    private RenderedChart cached(String key) {
        CachedChart value = cache.get(key);
        if (value == null || System.nanoTime() - value.createdAtNanos() >= CACHE_TTL_NANOS) {
            cache.remove(key);
            return null;
        }
        return value.chart();
    }

    private void putCache(String key, RenderedChart chart) {
        if (cache.size() >= MAX_CACHE_ENTRIES) {
            cache.entrySet().removeIf(entry ->
                    System.nanoTime() - entry.getValue().createdAtNanos() >= CACHE_TTL_NANOS);
            if (cache.size() >= MAX_CACHE_ENTRIES) {
                cache.keySet().stream().findFirst().ifPresent(cache::remove);
            }
        }
        cache.put(key, new CachedChart(chart, System.nanoTime()));
    }

    private void sendError(TelegramLongPollingBot bot, Long chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            bot.execute(message);
        } catch (TelegramApiException exception) {
            log.warn("Не вдалося повідомити чат {} про помилку аналітики", chatId, exception);
        }
    }

    private record CachedChart(RenderedChart chart, long createdAtNanos) {
    }
}
