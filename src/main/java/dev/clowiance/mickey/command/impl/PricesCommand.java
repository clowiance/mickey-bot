package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.helper.KeyboardHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class PricesCommand implements ICommand {

    private final KeyboardHelper keyboardHelper;

    @Override
    public String getCommand() {
        return "/prices";
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        try {
            SendMessage response = new SendMessage();
            response.setChatId(message.getChatId());
            response.setParseMode("HTML");
            response.setText("💰 <b>Вартість навчання в УКД — 2026</b>\n\nОберіть освітній рівень:");
            response.setReplyMarkup(keyboardHelper.catalogLevelMenu("prices"));
            bot.execute(response);
        } catch (TelegramApiException ignored) {
        }
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }
}
