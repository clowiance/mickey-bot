package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.PunishmentLog;
import dev.clowiance.mickey.entity.WarnEntity;
import dev.clowiance.mickey.helper.TargetHelper;
import dev.clowiance.mickey.helper.TelegramHtml;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.helper.TimeHelper;
import dev.clowiance.mickey.moderation.application.GroupMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCommandService {

    private final GroupMemberService groupMemberService;
    private final ModerationService moderationService;
    private final PunishmentService punishmentService;
    private final WarnService warnService;
    private final GuardService guardService;
    private final TargetHelper targetHelper;
    private final TimeHelper timeHelper;
    private final PermissionService permissionService;
    private final ActivityAuditService activityAuditService;
    private final dev.clowiance.mickey.helper.KeyboardHelper keyboardHelper;
    private final dev.clowiance.mickey.telegram.menu.MenuMessenger menuMessenger;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void handleWarn(TelegramLongPollingBot bot, Message message, String argsText) {
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
            List<WarnEntity> activeWarns = warnService.getActiveWarns(message.getChatId(), target.id());
            if (activeWarns.size() >= 3) {
                reply(bot, message, "⚠️ У користувача вже є максимальна кількість попереджень (3/3).");
                return;
            }

            WarnEntity createdWarn = warnService.addWarn(
                    message.getChatId(), target.id(), reason, message.getFrom().getId());
            int warns = warnService.getActiveWarns(message.getChatId(), target.id()).size();
            groupMemberService.setWarnCount(message.getChatId(), target.id(), target.username(), null, warns);
            punishmentService.log(message.getChatId(), target.id(), target.username(),
                    "WARN", reason, message.getFrom().getId());
            activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "MODERATION", "WARN",
                    String.format("Причина: %s | Адміністратор: %s | Попереджень: %d/3", reason, mentionAdmin(message), warns));

            String adminMention = mentionAdmin(message);
            String details = String.format(
                    "⚠️ <b>Попередження [#%d]</b>\n\n" +
                            "👤 <b>Порушник:</b> %s\n" +
                            "👮 <b>Адміністратор:</b> %s\n" +
                            "📊 <b>Кількість попереджень:</b> %d/3\n" +
                            "📝 <b>Причина:</b> %s",
                    createdWarn.getId(), target.mention(), adminMention, warns, TelegramHtml.escape(reason)
            );
            reply(bot, message, details);

            if (warns >= 3) {
                moderationService.banUser(bot, message.getChatId(), target.id(), null);
                groupMemberService.setBanned(message.getChatId(), target.id(), true);
                punishmentService.log(message.getChatId(), target.id(), target.username(),
                        "AUTO_BAN", "3 попередження", message.getFrom().getId());
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
            List<WarnEntity> activeWarns = warnService.getActiveWarns(message.getChatId(), target.id());
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

            int remainingWarns = warnService.getActiveWarns(message.getChatId(), target.id()).size();
            groupMemberService.setWarnCount(message.getChatId(), target.id(), target.username(), null, remainingWarns);
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
        }
    }

    public void handleMute(TelegramLongPollingBot bot, Message message, String argsText) {
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
        groupMemberService.setMuted(message.getChatId(), target.id(), true, mutedUntil);
        punishmentService.log(message.getChatId(), target.id(), target.username(),
                "MUTE", reason, message.getFrom().getId());

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
                target.mention(), adminMention, timeHelper.formatDuration(durationSeconds), TelegramHtml.escape(reason)
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
        groupMemberService.setMuted(message.getChatId(), target.id(), false, null);

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
        groupMemberService.setBanned(message.getChatId(), target.id(), true);
        punishmentService.log(message.getChatId(), target.id(), target.username(),
                "BAN", reason, message.getFrom().getId());

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
                target.mention(), adminMention, timeHelper.formatDuration(durationSeconds), TelegramHtml.escape(reason)
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
        groupMemberService.setBanned(message.getChatId(), target.id(), false);

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
        punishmentService.log(message.getChatId(), target.id(), target.username(),
                "KICK", reason, message.getFrom().getId());

        String adminMention = mentionAdmin(message);
        activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "MODERATION", "KICK",
                String.format("Причина: %s | Адміністратор: %s", reason, adminMention));
        String details = String.format(
                "👢 <b>Кік</b>\n\n" +
                        "👤 <b>Користувач:</b> %s\n" +
                        "👮 <b>Адміністратор:</b> %s\n" +
                        "📝 <b>Причина:</b> %s",
                target.mention(), adminMention, TelegramHtml.escape(reason)
        );
        reply(bot, message, details);
    }

    public void handleStats(TelegramLongPollingBot bot, Message message, String argsText) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, argsText);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        var member = groupMemberService.find(message.getChatId(), target.id());
        List<WarnEntity> activeWarns = warnService.getActiveWarns(message.getChatId(), target.id());
        List<PunishmentLog> history = punishmentService.history(message.getChatId(), target.id());

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
            sb.append("🔇 Замучений: ").append(member.muted() ? "так" : "ні").append("\n");
            sb.append("⛔ Забанений: ").append(member.banned() ? "так" : "ні").append("\n");
            if (member.joinedAt() != null) {
                sb.append("📅 У чаті з: ").append(member.joinedAt().format(FORMATTER)).append("\n");
            }
        }
        sb.append("\n📋 <b>Історія покарань:</b>\n");
        if (history.isEmpty()) {
            sb.append("Немає записів 🙌");
        } else {
            for (PunishmentLog log : history.stream().limit(5).toList()) {
                sb.append("• ").append(TelegramHtml.escape(log.getType())).append(" - ")
                        .append(TelegramHtml.escape(log.getReason()))
                        .append(" (").append(log.getCreatedAt().format(FORMATTER)).append(")\n");
            }
        }
        reply(bot, message, sb.toString());
    }

    /**
     * Панель адміністратора — це кнопки, а не простирадло тексту. Раніше сюди дописувався
     * увесь HELP_TEXT, тож /admin дублював /help цілком; тепер за розділами ведуть кнопки,
     * а повний перелік команд лишається за /help.
     */
    public void handleAdminPanel(TelegramLongPollingBot bot, Message message) {
        Long chatId = message.getChatId();
        boolean botIsAdmin = permissionService.isBotAdmin(bot, chatId);
        String text = TextHelper.ADMIN_PANEL_HEADER + "\n\n"
                + "🤖 Права бота: " + (botIsAdmin ? "✅ повні" : "❌ неповні/відсутні") + "\n"
                + "🆔 Chat ID: <code>" + chatId + "</code>\n\n"
                + "Обери розділ 👇";
        Long ownerId = message.getFrom() != null ? message.getFrom().getId() : null;
        menuMessenger.sendMenu(bot, chatId, ownerId, text, keyboardHelper.adminPanel());
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
        return user.getFirstName() != null ? TelegramHtml.escape(user.getFirstName()) : String.valueOf(user.getId());
    }

    private void reply(TelegramLongPollingBot bot, Message message, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(text);
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.warn("Не вдалося надіслати відповідь адмін-команди в чат {}", message.getChatId(), e);
        }
    }
}
