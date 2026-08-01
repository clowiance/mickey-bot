package dev.clowiance.mickey.service;

import dev.clowiance.mickey.helper.ContentHelper;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ContentHelper contentHelper;
    private final AntiSpamService antiSpamService;
    private final ChatMemberService chatMemberService;
    private final ModerationService moderationService;
    private final PunishmentService punishmentService;
    private final GuardService guardService;
    private final ActivityAuditService activityAuditService;

    public boolean moderate(TelegramLongPollingBot bot, Message message) {
        String text = message.getText() != null ? message.getText() : message.getCaption();
        if (text == null) {
            return false;
        }

        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        activityAuditService.logActivity(chatId, userId, message.getFrom().getUserName(), "MESSAGE", "MESSAGE", truncate(text, 300));

        if (guardService.isProtected(bot, chatId, userId)) {
            return false;
        }

        String violationTemplate = null;
        String reasonType = null;

        if (contentHelper.containsLink(text)) {
            violationTemplate = TextHelper.RULES_VIOLATION_LINK;
            reasonType = "посилання / реклама";
        } else if (contentHelper.containsProfanity(text)) {
            violationTemplate = TextHelper.RULES_VIOLATION_PROFANITY;
            reasonType = "нецензурна лексика";
        } else if (contentHelper.isSpam(text)) {
            violationTemplate = TextHelper.RULES_VIOLATION_SPAM;
            reasonType = "спам (капс / повтори / символи)";
        } else if (antiSpamService.isFastSending(userId)) {
            violationTemplate = TextHelper.RULES_VIOLATION_SPAM;
            reasonType = "надто швидке надсилання повідомлень (флуд)";
        } else if (antiSpamService.isDuplicateOrSimilarSpam(userId, text)) {
            violationTemplate = TextHelper.RULES_VIOLATION_SPAM;
            reasonType = "повторювані / схожі повідомлення (флуд)";
        }

        if (violationTemplate == null) {
            return false;
        }

        String displayName = mention(message);

        moderationService.deleteMessage(bot, chatId, message.getMessageId());

        int warns = chatMemberService.addWarn(userId);
        punishmentService.log(userId, message.getFrom().getUserName(), "WARN", reasonType, null);
        activityAuditService.logActivity(chatId, userId, message.getFrom().getUserName(), "MESSAGE", "VIOLATION",
                String.format("Причина: %s | Повідомлення видалено | Попереджень: %d/3", reasonType, warns));

        try {
            SendMessage notice = new SendMessage();
            notice.setChatId(chatId);
            notice.setParseMode("HTML");
            notice.setText(String.format(violationTemplate, displayName) + "\n\n" +
                    String.format(TextHelper.WARN_MESSAGE, displayName, warns, reasonType));
            bot.execute(notice);
        } catch (TelegramApiException ignored) {
        }

        if (warns >= 3) {
            moderationService.banUser(bot, chatId, userId, null);
            chatMemberService.setBanned(userId, true);
            punishmentService.log(userId, message.getFrom().getUserName(), "AUTO_BAN", "3 попередження", null);
            activityAuditService.logActivity(chatId, userId, message.getFrom().getUserName(), "MODERATION", "AUTO_BAN",
                    "Автоматичний бан після 3 попереджень (автомодерація)");
            try {
                SendMessage banNotice = new SendMessage();
                banNotice.setChatId(chatId);
                banNotice.setParseMode("HTML");
                banNotice.setText(String.format(TextHelper.AUTO_BAN_AFTER_WARNS, displayName));
                bot.execute(banNotice);
            } catch (TelegramApiException ignored) {
            }
        } else if (warns == 2) {
            long muteSeconds = 30 * 60L;
            moderationService.muteUser(bot, chatId, userId, muteSeconds);
            chatMemberService.setMuted(userId, true, LocalDateTime.now().plusSeconds(muteSeconds));
            punishmentService.log(userId, message.getFrom().getUserName(), "AUTO_MUTE", "2 попередження", null);
            activityAuditService.logActivity(chatId, userId, message.getFrom().getUserName(), "MODERATION", "AUTO_MUTE",
                    "Автоматичний мут на 30 хв після 2 попереджень (автомодерація)");
            try {
                SendMessage muteNotice = new SendMessage();
                muteNotice.setChatId(chatId);
                muteNotice.setParseMode("HTML");
                muteNotice.setText(String.format(TextHelper.MUTE_MESSAGE, displayName, 30, "2 попередження"));
                bot.execute(muteNotice);
            } catch (TelegramApiException ignored) {
            }
        }

        return true;
    }

    private String mention(Message message) {
        String username = message.getFrom().getUserName();
        String firstName = message.getFrom().getFirstName();
        return username != null ? "@" + username : firstName;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String singleLine = text.replaceAll("\\s+", " ").trim();
        return singleLine.length() > maxLength ? singleLine.substring(0, maxLength) + "…" : singleLine;
    }
}
