package dev.clowiance.mickey.service;

import dev.clowiance.mickey.helper.ContentHelper;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.moderation.application.GroupMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageModerationService {

    private final ContentHelper contentHelper;
    private final GroupMemberService groupMemberService;
    private final ModerationService moderationService;
    private final PunishmentService punishmentService;

    public boolean moderate(TelegramLongPollingBot bot, Message message) {
        String text = message.getText() != null ? message.getText() : message.getCaption();
        if (text == null) {
            return false;
        }

        String violationTemplate = null;
        String reasonType = null;

        if (contentHelper.containsLink(text)) {
            violationTemplate = TextHelper.RULES_VIOLATION_LINK;
            reasonType = "посилання";
        } else if (contentHelper.containsProfanity(text)) {
            violationTemplate = TextHelper.RULES_VIOLATION_PROFANITY;
            reasonType = "нецензурна лексика";
        } else if (contentHelper.isSpam(text)) {
            violationTemplate = TextHelper.RULES_VIOLATION_SPAM;
            reasonType = "спам";
        }

        if (violationTemplate == null) {
            return false;
        }

        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        String displayName = mention(message);

        moderationService.deleteMessage(bot, chatId, message.getMessageId());

        int warns = groupMemberService.addWarn(chatId, userId,
                message.getFrom().getUserName(), message.getFrom().getFirstName());
        punishmentService.log(chatId, userId, message.getFrom().getUserName(), "WARN", reasonType, null);

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
            moderationService.banUser(bot, chatId, userId);
            groupMemberService.setBanned(chatId, userId, true);
            punishmentService.log(chatId, userId, message.getFrom().getUserName(), "AUTO_BAN", "3 попередження", null);
            try {
                SendMessage banNotice = new SendMessage();
                banNotice.setChatId(chatId);
                banNotice.setParseMode("HTML");
                banNotice.setText(String.format(TextHelper.AUTO_BAN_AFTER_WARNS, displayName));
                bot.execute(banNotice);
            } catch (TelegramApiException ignored) {
            }
        } else if (warns == 2) {
            moderationService.muteUser(bot, chatId, userId, 30);
            groupMemberService.setMuted(chatId, userId, true, LocalDateTime.now().plusMinutes(30));
            punishmentService.log(chatId, userId, message.getFrom().getUserName(), "AUTO_MUTE", "2 попередження", null);
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
}
