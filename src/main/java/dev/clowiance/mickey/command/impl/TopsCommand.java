package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.entity.EconomyAccount;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.service.ChatMemberService;
import dev.clowiance.mickey.service.EconomyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TopsCommand implements ICommand {

    private final EconomyService economyService;
    private final ChatMemberService chatMemberService;

    @Override
    public String getCommand() {
        return "/tops";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        String category = args == null || args.isBlank() ? "balance" : args.trim().toLowerCase();
        List<EconomyAccount> top = economyService.leaderboard(10);

        if (top.isEmpty()) {
            send(bot, message.getChatId(), TextHelper.ECONOMY_TOP_EMPTY);
            return;
        }

        StringBuilder sb = new StringBuilder(String.format(TextHelper.ECONOMY_TOP_HEADER, category));
        int place = 1;
        for (EconomyAccount account : top) {
            var member = chatMemberService.findById(account.getUserId());
            String name = null;
            if (member != null) {
                if (member.getUsername() != null && !member.getUsername().isBlank()) {
                    name = "@" + member.getUsername();
                } else if (member.getFirstName() != null && !member.getFirstName().isBlank()) {
                    name = member.getFirstName();
                }
            }

            if (name == null || name.isBlank()) {
                continue;
            }

            long value = "earned".equals(category) ? account.getTotalEarned() : account.getBalance();
            sb.append(place).append(". ").append(name).append(" - ").append(value).append(" крон\n");
            place++;
        }

        if (place == 1) {
            send(bot, message.getChatId(), TextHelper.ECONOMY_TOP_EMPTY);
            return;
        }

        send(bot, message.getChatId(), sb.toString());
    }

    private void send(TelegramLongPollingBot bot, Long chatId, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("HTML");
            sendMessage.setText(text);
            bot.execute(sendMessage);
        } catch (TelegramApiException exception) {
            dev.clowiance.mickey.telegram.TelegramFailures.log("tops-reply", chatId, exception);
        }
    }
}
