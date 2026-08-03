package dev.clowiance.mickey.scheduler;

import dev.clowiance.mickey.administration.application.ChatAdministrationService;
import dev.clowiance.mickey.administration.domain.ChatFeature;
import dev.clowiance.mickey.entity.ScheduledBroadcast;
import dev.clowiance.mickey.helper.TelegramHtml;
import dev.clowiance.mickey.handler.MickeyHandler;
import dev.clowiance.mickey.service.BroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastScheduler {

    private final BroadcastService broadcastService;
    private final ChatAdministrationService chatAdministrationService;
    private final MickeyHandler bot;

    @Scheduled(fixedDelayString = "${bot.broadcast-poll-ms:60000}",
            initialDelayString = "${bot.scheduler-initial-delay-ms:30000}")
    public void sendDueBroadcasts() {
        for (ScheduledBroadcast broadcast : broadcastService.claimDue(50)) {
            if (!chatAdministrationService.isEnabled(broadcast.getChatId(), ChatFeature.BROADCASTS)) {
                broadcastService.cancel(broadcast, "Розсилки вимкнені для цього чату");
                continue;
            }
            try {
                SendMessage message = new SendMessage();
                message.setChatId(broadcast.getChatId());
                message.setText("📢 <b>Новини</b>\n\n" + TelegramHtml.escape(broadcast.getText()));
                message.setParseMode("HTML");
                bot.execute(message);
                broadcastService.markSent(broadcast);
            } catch (Exception e) {
                broadcastService.markFailed(broadcast, e);
                log.error("Не вдалося відправити заплановану розсилку {}", broadcast.getId(), e);
            }
        }
    }
}
