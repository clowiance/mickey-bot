package dev.clowiance.mickey.config;

import dev.clowiance.mickey.handler.MickeyHandler;
import dev.clowiance.mickey.service.PermissionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {

    private final MickeyHandler mickeyHandler;
    private final PermissionService permissionService;

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(mickeyHandler);
            var me = mickeyHandler.execute(new GetMe());
            permissionService.setBotUserId(me.getId());
<<<<<<< HEAD
            log.info("бота @{} успішно зарегало і поллінг тоже почався да", mickeyHandler.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("не повезло зарегати бота", e);
            throw new IllegalStateException("не повезло зарегати бота але експешином кидаю", e);
=======
            log.info("бота @{} успішно зарегано і поллінг тоже почався да", mickeyHandler.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("не повезло зарегати бота", e);
            throw new IllegalStateException("не повезло зарегати бота але вже експешином кидаю", e);
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
        }
    }
}
