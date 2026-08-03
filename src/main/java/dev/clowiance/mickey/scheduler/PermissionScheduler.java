package dev.clowiance.mickey.scheduler;

import dev.clowiance.mickey.entity.ChatInfo;
import dev.clowiance.mickey.handler.MickeyHandler;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionScheduler {

    private final PermissionService permissionService;
    private final MickeyHandler bot;

<<<<<<< HEAD
    @Scheduled(fixedDelay = 60000)
=======
    @Scheduled(fixedDelay = 300000)
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
    public void checkChats() {
        List<ChatInfo> chats = permissionService.findAllChats();
        for (ChatInfo chat : chats) {
            boolean isAdmin = permissionService.isBotAdmin(bot, chat.getChatId());
            permissionService.updateAdminStatus(chat.getChatId(), isAdmin);
            if (!isAdmin && permissionService.shouldWarn(chat.getChatId())) {
                try {
                    SendMessage sm = new SendMessage();
                    sm.setChatId(chat.getChatId());
                    sm.setParseMode("HTML");
                    sm.setText(TextHelper.ADMIN_REQUIRED);
                    bot.execute(sm);
                    permissionService.updateWarningTime(chat.getChatId());
                } catch (TelegramApiException e) {
                    log.error("не вдалося надіслати нагадування про адмін-права в чат {}", chat.getChatId(), e);
                }
            }
        }
    }
}
