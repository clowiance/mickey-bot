package dev.clowiance.mickey.config;

import dev.clowiance.mickey.handler.MickeyHandler;
import dev.clowiance.mickey.service.PermissionService;
import dev.clowiance.mickey.telegram.TelegramCommandRegistrar;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "bot", name = "polling-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class TelegramBotConfig {

    private final MickeyHandler mickeyHandler;
    private final PermissionService permissionService;
    private final TelegramCommandRegistrar commandRegistrar;

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(mickeyHandler);
            var me = mickeyHandler.execute(new GetMe());
            permissionService.setBotUserId(me.getId());
            commandRegistrar.register(mickeyHandler);
            log.info("бота @{} успішно зарегало і поллінг тоже почався да", mickeyHandler.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("не повезло зарегати бота", e);
            throw new IllegalStateException("не повезло зарегати бота але експешином кидаю", e);
        }
    }
}
