package dev.clowiance.mickey.service;

import dev.clowiance.mickey.config.BotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuardService {

    private final BotProperties botProperties;

    public boolean isProtected(TelegramLongPollingBot bot, Long chatId, Long userId) {
        if (userId == null) {
            return false;
        }
        if (botProperties.getDevIds() != null && botProperties.getDevIds().contains(userId)) {
            return true;
        }
        try {
            GetChatMember gcm = new GetChatMember();
            gcm.setChatId(chatId);
            gcm.setUserId(userId);
            var member = bot.execute(gcm);
            return member instanceof ChatMemberOwner || member instanceof ChatMemberAdministrator;
        } catch (TelegramApiException e) {
            return false;
        }
    }
}
