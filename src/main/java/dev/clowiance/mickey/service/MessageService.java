package dev.clowiance.mickey.service;

import dev.clowiance.mickey.helper.ContentHelper;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.helper.TelegramHtml;
import dev.clowiance.mickey.moderation.application.GroupMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    @Value("${audit.store-message-content:false}")
    private boolean storeMessageContent;

    private final ContentHelper contentHelper;
    private final AntiSpamService antiSpamService;
    private final GroupMemberService groupMemberService;
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

        String auditDetails = storeMessageContent
                ? truncate(text, 300)
                : "Отримано повідомлення, символів: " + text.length();
        activityAuditService.logActivity(chatId, userId, message.getFrom().getUserName(),
                "MESSAGE", "MESSAGE", auditDetails);

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
        } else if (antiSpamService.isFastSending(chatId, userId)) {
            violationTemplate = TextHelper.RULES_VIOLATION_SPAM;
            reasonType = "надто швидке надсилання повідомлень (флуд)";
        } else if (antiSpamService.isDuplicateOrSimilarSpam(chatId, userId, text)) {
            violationTemplate = TextHelper.RULES_VIOLATION_SPAM;
            reasonType = "повторювані / схожі повідомлення (флуд)";
        }

        if (violationTemplate == null) {
            return false;
        }

        String displayName = mention(message);

        moderationService.deleteMessage(bot, chatId, message.getMessageId());

        int warns = groupMemberService.addWarn(
                chatId, userId, message.getFrom().getUserName(), message.getFrom().getFirstName());
        punishmentService.log(chatId, userId, message.getFrom().getUserName(), "WARN", reasonType, null);
        activityAuditService.logActivity(chatId, userId, message.getFrom().getUserName(), "MESSAGE", "VIOLATION",
                String.format("Причина: %s | Повідомлення видалено | Попереджень: %d/3", reasonType, warns));

        try {
            SendMessage notice = new SendMessage();
            notice.setChatId(chatId);
            notice.setParseMode("HTML");
            notice.setText(String.format(violationTemplate, displayName) + "\n\n" +
                    String.format(TextHelper.WARN_MESSAGE, displayName, warns, reasonType));
            bot.execute(notice);
        } catch (TelegramApiException e) {
            log.warn("Не вдалося надіслати повідомлення про порушення в чат {}", chatId, e);
        }

        if (warns >= 3) {
            moderationService.banUser(bot, chatId, userId, null);
            groupMemberService.setBanned(chatId, userId, true);
            punishmentService.log(chatId, userId, message.getFrom().getUserName(),
                    "AUTO_BAN", "3 попередження", null);
            activityAuditService.logActivity(chatId, userId, message.getFrom().getUserName(), "MODERATION", "AUTO_BAN",
                    "Автоматичний бан після 3 попереджень (автомодерація)");
            try {
                SendMessage banNotice = new SendMessage();
                banNotice.setChatId(chatId);
                banNotice.setParseMode("HTML");
                banNotice.setText(String.format(TextHelper.AUTO_BAN_AFTER_WARNS, displayName));
                bot.execute(banNotice);
            } catch (TelegramApiException e) {
                log.warn("Не вдалося надіслати повідомлення про автоматичний бан у чат {}", chatId, e);
            }
        } else if (warns == 2) {
            long muteSeconds = 30 * 60L;
            moderationService.muteUser(bot, chatId, userId, muteSeconds);
            groupMemberService.setMuted(chatId, userId, true, LocalDateTime.now().plusSeconds(muteSeconds));
            punishmentService.log(chatId, userId, message.getFrom().getUserName(),
                    "AUTO_MUTE", "2 попередження", null);
            activityAuditService.logActivity(chatId, userId, message.getFrom().getUserName(), "MODERATION", "AUTO_MUTE",
                    "Автоматичний мут на 30 хв після 2 попереджень (автомодерація)");
            try {
                SendMessage muteNotice = new SendMessage();
                muteNotice.setChatId(chatId);
                muteNotice.setParseMode("HTML");
                muteNotice.setText(String.format(TextHelper.MUTE_MESSAGE, displayName, 30, "2 попередження"));
                bot.execute(muteNotice);
            } catch (TelegramApiException e) {
                log.warn("Не вдалося надіслати повідомлення про автоматичний мут у чат {}", chatId, e);
            }
        }

        return true;
    }

    private String mention(Message message) {
        String username = message.getFrom().getUserName();
        String firstName = message.getFrom().getFirstName();
        return username != null ? "@" + TelegramHtml.escape(username) : TelegramHtml.escape(firstName);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String singleLine = text.replaceAll("\\s+", " ").trim();
        return singleLine.length() > maxLength ? singleLine.substring(0, maxLength) + "…" : singleLine;
    }
}
