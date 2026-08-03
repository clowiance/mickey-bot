package dev.clowiance.mickey.scheduler;

import dev.clowiance.mickey.entity.ChatInfo;
import dev.clowiance.mickey.handler.MickeyHandler;
import dev.clowiance.mickey.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionScheduler {

    private final PermissionService permissionService;
    private final MickeyHandler bot;

    @Scheduled(fixedDelayString = "${bot.permission-refresh-ms:300000}",
            initialDelayString = "${bot.scheduler-initial-delay-ms:30000}")
    public void checkChats() {
        List<ChatInfo> chats = permissionService.findAllChats();
        for (ChatInfo chat : chats) {
            permissionService.refreshBotPermissions(bot, chat.getChatId());
        }
    }
}
