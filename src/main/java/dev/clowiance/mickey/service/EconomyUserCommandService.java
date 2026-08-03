package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.ChatMember;
import dev.clowiance.mickey.entity.EconomyAccount;
import dev.clowiance.mickey.helper.TargetHelper;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.helper.TimeHelper;
import dev.clowiance.mickey.moderation.application.GroupMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EconomyUserCommandService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ChatMemberService chatMemberService;
    private final GroupMemberService groupMemberService;
    private final EconomyService economyService;
    private final WarnService warnService;
    private final TargetHelper targetHelper;
    private final TimeHelper timeHelper;

    public void handleProfile(TelegramLongPollingBot bot, Message message, String argsText) {
        Long targetId;
        String targetUsername;
        String targetFirstName;

        if (argsText == null || argsText.isBlank()) {
            User from = message.getFrom();
            chatMemberService.touch(from);
            targetId = from.getId();
            targetUsername = from.getUserName();
            targetFirstName = from.getFirstName();
        } else {
            TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, argsText);
            if (!target.found()) {
                reply(bot, message, TextHelper.TARGET_NOT_FOUND);
                return;
            }
            targetId = target.id();
            targetUsername = target.username();
            targetFirstName = target.firstName();
        }

        ChatMember member = chatMemberService.findById(targetId);
        EconomyAccount account = economyService.getOrCreate(targetId);
        boolean groupChat = message.getChat() != null && !message.getChat().isUserChat();
        int activeWarns = groupChat
                ? warnService.getActiveWarns(message.getChatId(), targetId).size()
                : warnService.getActiveWarns(targetId).size();
        var groupState = groupChat ? groupMemberService.find(message.getChatId(), targetId) : null;

        String displayName = displayName(targetUsername, targetFirstName, targetId);

        StringBuilder sb = new StringBuilder();
        sb.append(TextHelper.PROFILE_HEADER).append("\n\n");
        sb.append("🪪 Ім'я: ").append(displayName).append("\n");
        sb.append("🆔 ID: <code>").append(targetId).append("</code>\n");
        if (member != null && member.getJoinedAt() != null) {
            sb.append("📅 Перша активність з ботом: ").append(member.getJoinedAt().format(FORMATTER)).append("\n");
        }
        if (member != null && member.getLastMessageAt() != null) {
            sb.append("💬 Востаннє писав: ").append(member.getLastMessageAt().format(FORMATTER)).append("\n");
        }
        sb.append("\n👑 Баланс: <b>").append(account.getBalance()).append(" крон</b>\n");
        sb.append("📈 Всього зароблено: ").append(account.getTotalEarned()).append(" крон\n");
        sb.append("🏆 Ранг: ").append(economyRank(account.getBalance())).append("\n");
        if (account.getDailyStreak() != null && account.getDailyStreak() > 0) {
            sb.append("🔥 Серія щоденних бонусів: ").append(account.getDailyStreak()).append(" дн.\n");
        }
        sb.append("\n⚠️ Активних попереджень: ").append(activeWarns).append("/3\n");
        if (groupState != null) {
            sb.append("🔇 Замучений у цій групі: ").append(bool(groupState.muted())).append("\n");
            sb.append("⛔ Забанений у цій групі: ").append(bool(groupState.banned())).append("\n");
        } else if (!groupChat) {
            sb.append("ℹ️ Статус модерації показується окремо в кожній групі.\n");
        }

        reply(bot, message, sb.toString());
    }

    public void handleDaily(TelegramLongPollingBot bot, Message message) {
        User from = message.getFrom();
        chatMemberService.touch(from);

        EconomyService.DailyResult result = economyService.claimDaily(from.getId());
        if (!result.success()) {
            long remainingSeconds = Math.max(1L, result.cooldownRemaining().getSeconds());
            reply(bot, message, String.format(TextHelper.DAILY_ALREADY_CLAIMED_TEMPLATE,
                    timeHelper.formatDuration(remainingSeconds)));
            return;
        }

        long newBalance = economyService.getBalance(from.getId());
        reply(bot, message, String.format(TextHelper.DAILY_SUCCESS_TEMPLATE, result.reward(), result.streak(), newBalance));
    }

    private String economyRank(long balance) {
        if (balance >= 10000) {
            return "👑 Легенда";
        }
        if (balance >= 5000) {
            return "💎 Багатій";
        }
        if (balance >= 1000) {
            return "🥇 Заможний";
        }
        if (balance >= 200) {
            return "🥈 Активний";
        }
        return "🌱 Новенький";
    }

    private String displayName(String username, String firstName, Long id) {
        if (username != null && !username.isBlank()) {
            return "@" + username;
        }
        if (firstName != null && !firstName.isBlank()) {
            return firstName;
        }
        return id != null ? String.valueOf(id) : "невідомий";
    }

    private String bool(Boolean value) {
        return Boolean.TRUE.equals(value) ? "так" : "ні";
    }

    private void reply(TelegramLongPollingBot bot, Message message, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(text);
            bot.execute(sendMessage);
        } catch (TelegramApiException exception) {
            dev.clowiance.mickey.telegram.TelegramFailures.log("economy-user-reply", message.getChatId(), exception);
        }
    }
}
