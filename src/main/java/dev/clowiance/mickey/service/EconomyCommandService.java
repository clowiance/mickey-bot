package dev.clowiance.mickey.service;

import dev.clowiance.mickey.entity.EconomyAccount;
import dev.clowiance.mickey.helper.TargetHelper;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EconomyCommandService {

    private final EconomyService economyService;
    private final TargetHelper targetHelper;
    private final ActivityAuditService activityAuditService;
    private final ChatMemberService chatMemberService;

    public void handleEconomy(TelegramLongPollingBot bot, Message message, String argsText) {
        String safeArgs = argsText == null ? "" : argsText.trim();
        if (safeArgs.isEmpty()) {
            reply(bot, message, TextHelper.ECONOMY_HELP);
            return;
        }

        String[] parts = safeArgs.split("\\s+", 2);
        String action = parts[0].toLowerCase();
        String rest = parts.length > 1 ? parts[1] : "";

        switch (action) {
            case "give", "add" -> handleGive(bot, message, rest);
            case "take", "remove" -> handleTake(bot, message, rest);
            case "set" -> handleSet(bot, message, rest);
            case "reset" -> handleReset(bot, message, rest);
            case "balance", "check" -> handleBalance(bot, message, rest);
            case "top", "leaderboard" -> handleTop(bot, message, rest);
            default -> reply(bot, message, TextHelper.ECONOMY_UNKNOWN_ACTION);
        }
    }

    private void handleGive(TelegramLongPollingBot bot, Message message, String rest) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, rest);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        String[] amountParts = splitAmountAndReason(target.remainingArgs());
        if (amountParts == null) {
            reply(bot, message, TextHelper.ECONOMY_AMOUNT_REQUIRED);
            return;
        }
        Long amount = parseAmount(amountParts[0]);
        if (amount == null || amount <= 0) {
            reply(bot, message, TextHelper.ECONOMY_INVALID_AMOUNT);
            return;
        }
        String reason = amountParts[1].isBlank() ? "не вказано" : amountParts[1];

        EconomyAccount account = economyService.deposit(target.id(), amount);
        activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "ECONOMY", "GIVE",
                String.format("Нараховано %d крон | Причина: %s | Розробник: %s", amount, reason, devMention(message)));
        reply(bot, message, String.format(TextHelper.ECONOMY_GIVE_SUCCESS, amount, target.mention(), account.getBalance()));
    }

    private void handleTake(TelegramLongPollingBot bot, Message message, String rest) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, rest);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        String[] amountParts = splitAmountAndReason(target.remainingArgs());
        if (amountParts == null) {
            reply(bot, message, TextHelper.ECONOMY_AMOUNT_REQUIRED);
            return;
        }
        Long amount = parseAmount(amountParts[0]);
        if (amount == null || amount <= 0) {
            reply(bot, message, TextHelper.ECONOMY_INVALID_AMOUNT);
            return;
        }
        String reason = amountParts[1].isBlank() ? "не вказано" : amountParts[1];

        EconomyAccount account = economyService.withdraw(target.id(), amount);
        activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "ECONOMY", "TAKE",
                String.format("Списано %d крон | Причина: %s | Розробник: %s", amount, reason, devMention(message)));
        reply(bot, message, String.format(TextHelper.ECONOMY_TAKE_SUCCESS, amount, target.mention(), account.getBalance()));
    }

    private void handleSet(TelegramLongPollingBot bot, Message message, String rest) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, rest);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        String amountToken = target.remainingArgs() == null ? "" : target.remainingArgs().trim().split("\\s+")[0];
        Long amount = parseAmount(amountToken);
        if (amount == null || amount < 0) {
            reply(bot, message, TextHelper.ECONOMY_INVALID_AMOUNT);
            return;
        }

        economyService.setBalance(target.id(), amount);
        activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "ECONOMY", "SET",
                String.format("Встановлено баланс %d крон | Розробник: %s", amount, devMention(message)));
        reply(bot, message, String.format(TextHelper.ECONOMY_SET_SUCCESS, target.mention(), amount));
    }

    private void handleReset(TelegramLongPollingBot bot, Message message, String rest) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, rest);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        economyService.reset(target.id());
        activityAuditService.logActivity(message.getChatId(), target.id(), target.username(), "ECONOMY", "RESET",
                "Рахунок обнулено | Розробник: " + devMention(message));
        reply(bot, message, String.format(TextHelper.ECONOMY_RESET_SUCCESS, target.mention()));
    }

    private void handleBalance(TelegramLongPollingBot bot, Message message, String rest) {
        TargetHelper.ResolvedTarget target = targetHelper.resolve(bot, message, rest);
        if (!target.found()) {
            reply(bot, message, TextHelper.TARGET_NOT_FOUND);
            return;
        }
        long balance = economyService.getBalance(target.id());
        reply(bot, message, String.format(TextHelper.ECONOMY_BALANCE_INFO, target.mention(), balance));
    }

    private void handleTop(TelegramLongPollingBot bot, Message message, String rest) {
        int limit = 10;
        if (rest != null && !rest.isBlank()) {
            try {
                limit = Integer.parseInt(rest.trim().split("\\s+")[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        List<EconomyAccount> top = economyService.leaderboard(limit);
        if (top.isEmpty()) {
            reply(bot, message, TextHelper.ECONOMY_TOP_EMPTY);
            return;
        }

        StringBuilder sb = new StringBuilder(TextHelper.ECONOMY_TOP_HEADER).append("\n");
        int place = 1;
        for (EconomyAccount account : top) {
            var member = chatMemberService.findById(account.getUserId());
            String name = "Користувач #" + account.getUserId();
            if (member != null) {
                if (member.getUsername() != null && !member.getUsername().isBlank()) {
                    name = "@" + member.getUsername();
                } else if (member.getFirstName() != null && !member.getFirstName().isBlank()) {
                    name = member.getFirstName();
                }
            }
            sb.append(place).append(". ").append(name).append(" - ").append(account.getBalance()).append(" крон\n");
            place++;
        }
        reply(bot, message, sb.toString());
    }

    private String[] splitAmountAndReason(String remaining) {
        if (remaining == null || remaining.isBlank()) {
            return null;
        }
        String[] parts = remaining.trim().split("\\s+", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }

    private Long parseAmount(String token) {
        try {
            long value = Long.parseLong(token.trim());
            return value;
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private String devMention(Message message) {
        var user = message.getFrom();
        if (user.getUserName() != null && !user.getUserName().isBlank()) {
            return "@" + user.getUserName();
        }
        return user.getFirstName() != null ? user.getFirstName() : String.valueOf(user.getId());
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
}
