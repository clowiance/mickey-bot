package dev.clowiance.mickey.telegram;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TelegramFailures {
    private TelegramFailures() {
    }

    public static void log(String operation, Long chatId, Exception exception) {
        log.warn("Telegram operation '{}' failed for chat {}", operation, chatId, exception);
    }
}
