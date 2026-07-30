package dev.clowiance.mickey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationService {

    public void muteUser(TelegramLongPollingBot bot, Long chatId, Long userId, int minutes) {
        try {
            RestrictChatMember restrict = new RestrictChatMember();
            restrict.setChatId(chatId);
            restrict.setUserId(userId);
            restrict.setUntilDate((int) LocalDateTime.now().plusMinutes(minutes).toEpochSecond(ZoneOffset.UTC));
            ChatPermissions permissions = new ChatPermissions();
            permissions.setCanSendMessages(false);
            permissions.setCanSendMediaMessages(false);
            permissions.setCanSendPolls(false);
            permissions.setCanSendOtherMessages(false);
            restrict.setPermissions(permissions);
            bot.execute(restrict);
            log.info("користувача {} замучено на {} хв у чаті {}", userId, minutes, chatId);
        } catch (TelegramApiException e) {
            log.error("порушнику повезло його не замутять {}", userId, e);
        }
    }

    public void unmuteUser(TelegramLongPollingBot bot, Long chatId, Long userId) {
        try {
            RestrictChatMember restrict = new RestrictChatMember();
            restrict.setChatId(chatId);
            restrict.setUserId(userId);
            ChatPermissions permissions = new ChatPermissions();
            permissions.setCanSendMessages(true);
            permissions.setCanSendMediaMessages(true);
            permissions.setCanSendPolls(true);
            permissions.setCanSendOtherMessages(true);
            permissions.setCanAddWebPagePreviews(true);
            restrict.setPermissions(permissions);
            bot.execute(restrict);
            log.info("з користувача {} знято мут у чаті {}", userId, chatId);
        } catch (TelegramApiException e) {
            log.error("порушнику не повезло йому не можна зняти мут {}", userId, e);
        }
    }

    public void banUser(TelegramLongPollingBot bot, Long chatId, Long userId) {
        try {
            BanChatMember ban = new BanChatMember();
            ban.setChatId(chatId);
            ban.setUserId(userId);
            bot.execute(ban);
            log.info("користувача {} забанено у чаті {}", userId, chatId);
        } catch (TelegramApiException e) {
            log.error("сьодня без банів {}", userId, e);
        }
    }

    public void unbanUser(TelegramLongPollingBot bot, Long chatId, Long userId) {
        try {
            UnbanChatMember unban = new UnbanChatMember();
            unban.setChatId(chatId);
            unban.setUserId(userId);
            unban.setOnlyIfBanned(true);
            bot.execute(unban);
            log.info("користувача {} розбанено у чаті {}", userId, chatId);
        } catch (TelegramApiException e) {
            log.error("ггвп порушника не вдалось розбанити {}", userId, e);
        }
    }

    public void deleteMessage(TelegramLongPollingBot bot, Long chatId, Integer messageId) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(messageId);
            bot.execute(deleteMessage);
            log.info("повідомлення {} видалено у чаті {}", messageId, chatId);
        } catch (TelegramApiException e) {
            log.error("повідомлення вже частина корабля його не можна видаляти {}", messageId, e);
        }
    }
}
