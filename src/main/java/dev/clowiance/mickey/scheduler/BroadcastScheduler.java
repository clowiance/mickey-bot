package dev.clowiance.mickey.scheduler;

import dev.clowiance.mickey.entity.ScheduledBroadcast;
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
    private final MickeyHandler bot;

    @Scheduled(fixedDelay = 60000)
    public void sendDueBroadcasts() {
        for (ScheduledBroadcast broadcast : broadcastService.findDue()) {
            try {
                SendMessage message = new SendMessage();
                message.setChatId(broadcast.getChatId());
                message.setText("📢 <b>Новини приймальної комісії</b>\n\n" + broadcast.getText());
                message.setParseMode("HTML");
                bot.execute(message);
                broadcastService.markSent(broadcast);
            } catch (TelegramApiException e) {
                log.error("шота не вийшло відправити заплановану розсилку {}", broadcast.getId(), e);
            }
        }
    }
}
