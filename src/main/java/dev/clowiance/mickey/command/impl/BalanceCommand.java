package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.service.EconomyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class BalanceCommand implements ICommand {

    private final EconomyService economyService;

    @Override
    public String getCommand() {
        return "/balance";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    /**
     * Свій баланс дивиться будь-хто — це користувацька команда поряд із /daily та /profile.
     * Позначка «лише для розробників» відмовляла в ній усім учасникам, хоч /help її обіцяв.
     */
    @Override
    public boolean isInfoCommand() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        long balance = economyService.getBalance(message.getFrom().getId());
        send(bot, message.getChatId(), String.format(TextHelper.ECONOMY_BALANCE_INFO, balance));
    }

    private void send(TelegramLongPollingBot bot, Long chatId, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setParseMode("HTML");
            sendMessage.setText(text);
            bot.execute(sendMessage);
        } catch (TelegramApiException exception) {
            dev.clowiance.mickey.telegram.TelegramFailures.log("balance-reply", chatId, exception);
        }
    }
}
