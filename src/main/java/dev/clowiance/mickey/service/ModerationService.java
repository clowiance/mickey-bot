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

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationService {

    public void muteUser(TelegramLongPollingBot bot, Long chatId, Long userId, Long durationSeconds) {
        try {
            RestrictChatMember restrict = new RestrictChatMember();
            restrict.setChatId(chatId);
            restrict.setUserId(userId);
            if (durationSeconds != null && durationSeconds > 0) {
                restrict.setUntilDate((int) Instant.now().plusSeconds(durationSeconds).getEpochSecond());
            } else {
                restrict.setUntilDate(0);
            }
            ChatPermissions permissions = new ChatPermissions();
            permissions.setCanSendMessages(false);
            permissions.setCanSendMediaMessages(false);
            permissions.setCanSendPolls(false);
            permissions.setCanSendOtherMessages(false);
            permissions.setCanAddWebPagePreviews(false);
            restrict.setPermissions(permissions);
            bot.execute(restrict);
            log.info("користувача {} замучено у чаті {} на {} сек", userId, chatId, durationSeconds);
        } catch (TelegramApiException e) {
            log.error("порушнику повезло його не замутять {}", userId, e);
        }
    }

    public void unmuteUser(TelegramLongPollingBot bot, Long chatId, Long userId) {
        try {
            RestrictChatMember restrict = new RestrictChatMember();
            restrict.setChatId(chatId);
            restrict.setUserId(userId);
            restrict.setUntilDate(0);
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

    public void banUser(TelegramLongPollingBot bot, Long chatId, Long userId, Long durationSeconds) {
        try {
            BanChatMember ban = new BanChatMember();
            ban.setChatId(chatId);
            ban.setUserId(userId);
            if (durationSeconds != null && durationSeconds > 0) {
                ban.setUntilDate((int) Instant.now().plusSeconds(durationSeconds).getEpochSecond());
            } else {
                ban.setUntilDate(0);
            }
            bot.execute(ban);
            log.info("користувача {} забанено у чаті {} на {} сек", userId, chatId, durationSeconds);
        } catch (TelegramApiException e) {
            log.error("сьодня без банів {}", userId, e);
        }
    }

    public void unBanUser(TelegramLongPollingBot bot, Long chatId, Long userId) {
        try {
            UnbanChatMember unBan = new UnbanChatMember();
            unBan.setChatId(chatId);
            unBan.setUserId(userId);
            unBan.setOnlyIfBanned(false);
            bot.execute(unBan);
            log.info("користувача {} розбанено у чаті {}", userId, chatId);
        } catch (TelegramApiException e) {
            log.error("ггвп порушника не вдалось розбанити {}", userId, e);
        }
    }

    public void kickUser(TelegramLongPollingBot bot, Long chatId, Long userId) {
        try {
            BanChatMember ban = new BanChatMember();
            ban.setChatId(chatId);
            ban.setUserId(userId);
            ban.setUntilDate(0);
            bot.execute(ban);
            UnbanChatMember unBan = new UnbanChatMember();
            unBan.setChatId(chatId);
            unBan.setUserId(userId);
            unBan.setOnlyIfBanned(false);
            bot.execute(unBan);
            log.info("користувача {} кікнуто з чату {}", userId, chatId);
        } catch (TelegramApiException e) {
            log.error("не вдалося кікнути користувача {}", userId, e);
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
