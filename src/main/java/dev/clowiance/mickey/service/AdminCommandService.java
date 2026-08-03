package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.ChatMember;
import dev.clowiance.mickey.entity.PunishmentLog;
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
import dev.clowiance.mickey.entity.WarnEntity;
import dev.clowiance.mickey.helper.TargetHelper;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.helper.TimeHelper;
<<<<<<< HEAD
=======
import dev.clowiance.mickey.helper.TextHelper;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
<<<<<<< HEAD
<<<<<<< HEAD
=======
import org.telegram.telegrambots.meta.api.objects.User;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
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
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
    private final WarnService warnService;
    private final GuardService guardService;
    private final TargetHelper targetHelper;
    private final TimeHelper timeHelper;
    private final PermissionService permissionService;
    private final ActivityAuditService activityAuditService;
<<<<<<< HEAD
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void handleWarn(TelegramLongPollingBot bot, Message message, String argsText) {
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, argsText);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        if (target.id().equals(message.getFrom().getId())) {
            reply(bot, message, "❌ Ви не можете дати попередження самому собі.");
            return;
        }
        if (isProtected(bot, message, target.id())) {
            return;
        }

        String reason = target.remainingArgs().isBlank() ? "не вказано" : target.remainingArgs();

        synchronized (this) {
            List<WarnEntity> activeWarns = warnService.getActiveWarns(target.id());
            if (activeWarns.size() >= 3) {
                reply(bot, message, "⚠️ У користувача вже є максимальна кількість попереджень (3/3).");
                return;
            }

            WarnEntity createdWarn = warnService.addWarn(target.id(), reason, message.getFrom().getId());
            int warns = warnService.getActiveWarns(target.id()).size();
            punishmentService.log(target.id(), target.username(), "WARN", reason, message.getFrom().getId());
            activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "MODERATION", "WARN",
                    String.format("Причина: %s | Адміністратор: %s | Попереджень: %d/3", reason, mentionAdmin(message), warns));

            String adminMention = mentionAdmin(message);
            String details = String.format(
                    "⚠️ <b>Попередження [#%d]</b>\n\n" +
                            "👤 <b>Порушник:</b> %s\n" +
                            "👮 <b>Адміністратор:</b> %s\n" +
                            "📊 <b>Кількість попереджень:</b> %d/3\n" +
                            "📝 <b>Причина:</b> %s",
                    createdWarn.getId(), target.mention(), adminMention, warns, reason
            );
            reply(bot, message, details);

            if (warns >= 3) {
                moderationService.banUser(bot, message.getChatId(), target.id(), null);
                chatMemberService.setBanned(target.id(), true);
                punishmentService.log(target.id(), target.username(), "AUTO_BAN", "3 попередження", message.getFrom().getId());
                activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "MODERATION", "AUTO_BAN",
                        "Автоматичний бан після 3 попереджень | Адміністратор: " + mentionAdmin(message));
                reply(bot, message, String.format(TextHelper.AUTO_BAN_AFTER_WARNS, target.mention()));
            }
        }
    }

    public void handleUnwarn(TelegramLongPollingBot bot, Message message, String argsText) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, argsText);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }

        String remaining = target.remainingArgs();
        Long specificWarnId = null;
        if (remaining != null && !remaining.isBlank()) {
            try {
                specificWarnId = Long.parseLong(remaining.trim());
            } catch (NumberFormatException ignored) {}
        }

        final Long finalSpecificWarnId = specificWarnId;

        synchronized (this) {
            List<WarnEntity> activeWarns = warnService.getActiveWarns(target.id());
            if (activeWarns.isEmpty()) {
                reply(bot, message, "❌ У цього користувача немає активних попереджень.");
                return;
            }

            WarnEntity targetWarn;
            if (finalSpecificWarnId != null) {
                targetWarn = activeWarns.stream().filter(w -> w.getId().equals(finalSpecificWarnId)).findFirst().orElse(null);
                if (targetWarn == null) {
                    reply(bot, message, String.format("❌ Попередження з ID #%d не знайдено серед активних у цього користувача.", finalSpecificWarnId));
                    return;
                }
                warnService.removeWarnById(targetWarn.getId());
            } else {
                targetWarn = activeWarns.get(activeWarns.size() - 1);
                warnService.removeWarnById(targetWarn.getId());
            }

            int remainingWarns = warnService.getActiveWarns(target.id()).size();
            String adminMention = mentionAdmin(message);
            activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "MODERATION", "UNWARN",
                    String.format("Знято попередження #%d | Адміністратор: %s | Залишилось: %d/3",
                            targetWarn.getId(), adminMention, remainingWarns));
            String details = String.format(
                    "🔄 <b>Зняття попередження [#%d]</b>\n\n" +
                            "👤 <b>Користувач:</b> %s\n" +
                            "👮 <b>Адміністратор:</b> %s\n" +
                            "📊 <b>Попереджень залишилось:</b> %d/3",
                    targetWarn.getId(), target.mention(), adminMention, remainingWarns
            );
            reply(bot, message, details);
<<<<<<< HEAD
=======
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
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
        }
    }

    public void handleMute(TelegramLongPollingBot bot, Message message, String argsText) {
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, argsText);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        if (target.id().equals(message.getFrom().getId())) {
            reply(bot, message, "❌ Ви не можете застосувати мут до самого себе.");
            return;
        }
        if (isProtected(bot, message, target.id())) {
            return;
        }

        String[] parts = target.remainingArgs().isBlank() ? new String[0] : target.remainingArgs().split("\\s+", 2);
        long durationSeconds = 60 * 60L;
        String reason = "не вказано";

        if (parts.length > 0 && timeHelper.looksLikeDuration(parts[0])) {
            Long parsed = timeHelper.parseDurationSeconds(parts[0]);
            durationSeconds = parsed != null ? parsed : durationSeconds;
            reason = parts.length > 1 && !parts[1].isBlank() ? parts[1] : "не вказано";
        } else if (parts.length > 0) {
            reason = String.join(" ", parts).isBlank() ? "не вказано" : String.join(" ", parts);
        }

        Long restrictUntilSeconds = durationSeconds == TimeHelper.PERMANENT ? 0L : durationSeconds;
        moderationService.muteUser(bot, message.getChatId(), target.id(), restrictUntilSeconds);
        LocalDateTime mutedUntil = durationSeconds == TimeHelper.PERMANENT
                ? LocalDateTime.now().plusYears(100)
                : LocalDateTime.now().plusSeconds(durationSeconds);
        chatMemberService.setMuted(target.id(), true, mutedUntil);
        punishmentService.log(target.id(), target.username(), "MUTE", reason, message.getFrom().getId());

        String adminMention = mentionAdmin(message);
        activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "MODERATION", "MUTE",
                String.format("Тривалість: %s | Причина: %s | Адміністратор: %s",
                        timeHelper.formatDuration(durationSeconds), reason, adminMention));
        String details = String.format(
                "🔇 <b>Мут</b>\n\n" +
                        "👤 <b>Порушник:</b> %s\n" +
                        "👮 <b>Адміністратор:</b> %s\n" +
                        "⏳ <b>Тривалість:</b> %s\n" +
                        "📝 <b>Причина:</b> %s",
                target.mention(), adminMention, timeHelper.formatDuration(durationSeconds), reason
        );
        reply(bot, message, details);
    }

    public void handleUnMute(TelegramLongPollingBot bot, Message message, String argsText) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, argsText);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        moderationService.unmuteUser(bot, message.getChatId(), target.id());
        chatMemberService.setMuted(target.id(), false, null);

        String adminMention = mentionAdmin(message);
        activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "MODERATION", "UNMUTE",
                "Адміністратор: " + adminMention);
        String details = String.format(
                "🔊 <b>Розмут</b>\n\n" +
                        "👤 <b>Користувач:</b> %s\n" +
                        "👮 <b>Адміністратор:</b> %s",
                target.mention(), adminMention
        );
        reply(bot, message, details);
    }

    public void handleBan(TelegramLongPollingBot bot, Message message, String argsText) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, argsText);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        if (target.id().equals(message.getFrom().getId())) {
            reply(bot, message, "❌ Ви не можете забанити самого себе.");
            return;
        }
        if (isProtected(bot, message, target.id())) {
            return;
        }

        String[] parts = target.remainingArgs().isBlank() ? new String[0] : target.remainingArgs().split("\\s+", 2);
        long durationSeconds = TimeHelper.PERMANENT;
        String reason = "не вказано";

        if (parts.length > 0 && timeHelper.looksLikeDuration(parts[0])) {
            Long parsed = timeHelper.parseDurationSeconds(parts[0]);
            durationSeconds = parsed != null ? parsed : durationSeconds;
            reason = parts.length > 1 && !parts[1].isBlank() ? parts[1] : "не вказано";
        } else if (parts.length > 0) {
            reason = String.join(" ", parts).isBlank() ? "не вказано" : String.join(" ", parts);
        }

        Long banUntilSeconds = durationSeconds == TimeHelper.PERMANENT ? 0L : durationSeconds;
        moderationService.banUser(bot, message.getChatId(), target.id(), banUntilSeconds);
        chatMemberService.setBanned(target.id(), true);
        punishmentService.log(target.id(), target.username(), "BAN", reason, message.getFrom().getId());

        String adminMention = mentionAdmin(message);
        activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "MODERATION", "BAN",
                String.format("Тривалість: %s | Причина: %s | Адміністратор: %s",
                        timeHelper.formatDuration(durationSeconds), reason, adminMention));
        String details = String.format(
                "⛔ <b>Бан</b>\n\n" +
                        "👤 <b>Порушник:</b> %s\n" +
                        "👮 <b>Адміністратор:</b> %s\n" +
                        "⏳ <b>Тривалість:</b> %s\n" +
                        "📝 <b>Причина:</b> %s",
                target.mention(), adminMention, timeHelper.formatDuration(durationSeconds), reason
        );
        reply(bot, message, details);
    }

    public void handleUnBan(TelegramLongPollingBot bot, Message message, String argsText) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, argsText);
        if (target.id() == null) {
            reply(bot, message, TextHelper.INVALID_ID);
            return;
        }
        moderationService.unBanUser(bot, message.getChatId(), target.id());
        chatMemberService.setBanned(target.id(), false);

        String adminMention = mentionAdmin(message);
        activityAuditService.logActivity(message.getChatId(), target.id(), null, "MODERATION", "UNBAN",
                "Адміністратор: " + adminMention);
        String details = String.format(
                "✅ <b>Розбан</b>\n\n" +
                        "👤 <b>Користувач:</b> ID %d\n" +
                        "👮 <b>Адміністратор:</b> %s",
                target.id(), adminMention
        );
        reply(bot, message, details);
    }

    public void handleKick(TelegramLongPollingBot bot, Message message, String argsText) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, argsText);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        if (target.id().equals(message.getFrom().getId())) {
            reply(bot, message, "❌ Ви не можете кікнути самого себе.");
            return;
        }
        if (isProtected(bot, message, target.id())) {
            return;
        }
        String reason = target.remainingArgs().isBlank() ? "не вказано" : target.remainingArgs();
        moderationService.kickUser(bot, message.getChatId(), target.id());
        punishmentService.log(target.id(), target.username(), "KICK", reason, message.getFrom().getId());

        String adminMention = mentionAdmin(message);
        activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "MODERATION", "KICK",
                String.format("Причина: %s | Адміністратор: %s", reason, adminMention));
        String details = String.format(
                "👢 <b>Кік</b>\n\n" +
                        "👤 <b>Користувач:</b> %s\n" +
                        "👮 <b>Адміністратор:</b> %s\n" +
                        "📝 <b>Причина:</b> %s",
                target.mention(), adminMention, reason
        );
        reply(bot, message, details);
    }

    public void handleStats(TelegramLongPollingBot bot, Message message, String argsText) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, argsText);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        ChatMember member = chatMemberService.findById(target.id());
        List<WarnEntity> activeWarns = warnService.getActiveWarns(target.id());
        List<PunishmentLog> history = punishmentService.history(target.id());

        StringBuilder sb = new StringBuilder();
        sb.append("📈 <b>Статистика користувача</b> ").append(target.mention()).append("\n\n");
        sb.append("⚠️ Активних попереджень: ").append(activeWarns.size()).append("/3\n");
        if (!activeWarns.isEmpty()) {
            sb.append("   └ ID варнів: ");
            for (int i = 0; i < activeWarns.size(); i++) {
                sb.append("#").append(activeWarns.get(i).getId());
                if (i < activeWarns.size() - 1) sb.append(", ");
            }
            sb.append("\n");
        }
        if (member != null) {
<<<<<<< HEAD
=======
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
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
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

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
    public void handleAdminPanel(TelegramLongPollingBot bot, Message message) {
        Long chatId = message.getChatId();
        boolean botIsAdmin = permissionService.isBotAdmin(bot, chatId);
        StringBuilder sb = new StringBuilder();
        sb.append(TextHelper.ADMIN_PANEL_HEADER).append("\n\n");
        sb.append("🤖 Права бота: ").append(botIsAdmin ? "✅ повні" : "❌ неповні/відсутні").append("\n");
        sb.append("🆔 Chat ID: ").append(chatId).append("\n\n");
        sb.append(TextHelper.HELP_TEXT);
        reply(bot, message, sb.toString());
    }

    private boolean isProtected(TelegramLongPollingBot bot, Message message, Long targetId) {
        if (guardService.isProtected(bot, message.getChatId(), targetId)) {
            reply(bot, message, TextHelper.TARGET_PROTECTED);
            return true;
        }
        return false;
    }

    private String mentionAdmin(Message message) {
        var user = message.getFrom();
        if (user.getUserName() != null && !user.getUserName().isBlank()) {
            return "@" + user.getUserName();
        }
        return user.getFirstName() != null ? user.getFirstName() : String.valueOf(user.getId());
<<<<<<< HEAD
=======
    private boolean requireReply(TelegramLongPollingBot bot, Message message) {
        if (message.getReplyToMessage() == null) {
            reply(bot, message, TextHelper.REPLY_NEEDED);
            return false;
        }
        return true;
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
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
<<<<<<< HEAD
<<<<<<< HEAD
=======

    private String mention(User user) {
        return user.getUserName() != null ? "@" + user.getUserName() : user.getFirstName();
    }
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
}
