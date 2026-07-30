package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.ChatMember;
import dev.clowiance.mickey.entity.PunishmentLog;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCommandService {

    private final ChatMemberService chatMemberService;
    private final ModerationService moderationService;
    private final PunishmentService punishmentService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void handleWarn(TelegramLongPollingBot bot, Message message, String argsText) {
        if (!requireReply(bot, message)) {
            return;
        }
        User target = message.getReplyToMessage().getFrom();
        String reason = argsText.isBlank() ? "не вказано" : argsText;
        int warns = chatMemberService.addWarn(target.getId());
        punishmentService.log(target.getId(), target.getUserName(), "WARN", reason, message.getFrom().getId());
        reply(bot, message, String.format(TextHelper.WARN_MESSAGE, mention(target), warns, reason));

        if (warns >= 3) {
            moderationService.banUser(bot, message.getChatId(), target.getId());
            chatMemberService.setBanned(target.getId(), true);
            punishmentService.log(target.getId(), target.getUserName(), "AUTO_BAN", "3 попередження", message.getFrom().getId());
            reply(bot, message, String.format(TextHelper.AUTO_BAN_AFTER_WARNS, mention(target)));
        }
    }

    public void handleMute(TelegramLongPollingBot bot, Message message, String argsText) {
        if (!requireReply(bot, message)) {
            return;
        }
        User target = message.getReplyToMessage().getFrom();
        String[] parts = argsText.trim().split("\s+", 2);
        int minutes = 60;
        String reason = "не вказано";
        if (parts.length > 0 && !parts[0].isBlank()) {
            try {
                minutes = Integer.parseInt(parts[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        if (parts.length > 1) {
            reason = parts[1];
        }
        moderationService.muteUser(bot, message.getChatId(), target.getId(), minutes);
        chatMemberService.setMuted(target.getId(), true, LocalDateTime.now().plusMinutes(minutes));
        punishmentService.log(target.getId(), target.getUserName(), "MUTE", reason, message.getFrom().getId());
        reply(bot, message, String.format(TextHelper.MUTE_MESSAGE, mention(target), minutes, reason));
    }

    public void handleUnmute(TelegramLongPollingBot bot, Message message) {
        if (!requireReply(bot, message)) {
            return;
        }
        User target = message.getReplyToMessage().getFrom();
        moderationService.unmuteUser(bot, message.getChatId(), target.getId());
        chatMemberService.setMuted(target.getId(), false, null);
        reply(bot, message, "🔊 " + mention(target) + " знову може писати в чат!");
    }

    public void handleBan(TelegramLongPollingBot bot, Message message, String argsText) {
        if (!requireReply(bot, message)) {
            return;
        }
        User target = message.getReplyToMessage().getFrom();
        String reason = argsText.isBlank() ? "не вказано" : argsText;
        moderationService.banUser(bot, message.getChatId(), target.getId());
        chatMemberService.setBanned(target.getId(), true);
        punishmentService.log(target.getId(), target.getUserName(), "BAN", reason, message.getFrom().getId());
        reply(bot, message, String.format(TextHelper.BAN_MESSAGE, mention(target), reason));
    }

    public void handleUnban(TelegramLongPollingBot bot, Message message, String argsText) {
        try {
            Long targetId = Long.parseLong(argsText.trim());
            moderationService.unbanUser(bot, message.getChatId(), targetId);
            chatMemberService.setBanned(targetId, false);
            reply(bot, message, "✅ Користувача " + targetId + " розбанено!");
        } catch (NumberFormatException e) {
            reply(bot, message, "⚠️ Вкажи коректний ID користувача: /unban 123456789");
        }
    }

    public void handleStats(TelegramLongPollingBot bot, Message message) {
        if (!requireReply(bot, message)) {
            return;
        }
        User target = message.getReplyToMessage().getFrom();
        ChatMember member = chatMemberService.findById(target.getId());
        List<PunishmentLog> history = punishmentService.history(target.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("📈 <b>Статистика користувача</b> ").append(mention(target)).append("\n\n");
        if (member != null) {
            sb.append("⚠️ Попереджень: ").append(member.getWarnCount()).append("\n");
            sb.append("🔇 Замучений: ").append(Boolean.TRUE.equals(member.getMuted()) ? "так" : "ні").append("\n");
            sb.append("⛔ Забанений: ").append(Boolean.TRUE.equals(member.getBanned()) ? "так" : "ні").append("\n");
            if (member.getJoinedAt() != null) {
                sb.append("📅 У чаті з: ").append(member.getJoinedAt().format(FORMATTER)).append("\n");
            }
        }
        sb.append("\n📋 <b>Історія покарань:</b>\n");
        if (history.isEmpty()) {
            sb.append("Немає записів 🙌");
        } else {
            for (PunishmentLog log : history.stream().limit(5).toList()) {
                sb.append("• ").append(log.getType()).append(" - ").append(log.getReason())
                        .append(" (").append(log.getCreatedAt().format(FORMATTER)).append(")\n");
            }
        }
        reply(bot, message, sb.toString());
    }

    private boolean requireReply(TelegramLongPollingBot bot, Message message) {
        if (message.getReplyToMessage() == null) {
            reply(bot, message, TextHelper.REPLY_NEEDED);
            return false;
        }
        return true;
    }

    private void reply(TelegramLongPollingBot bot, Message message, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(text);
            bot.execute(sendMessage);
        } catch (TelegramApiException ignored) {
        }
    }

    private String mention(User user) {
        return user.getUserName() != null ? "@" + user.getUserName() : user.getFirstName();
    }
}
