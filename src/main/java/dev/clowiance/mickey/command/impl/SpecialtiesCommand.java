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
public class SpecialtiesCommand implements ICommand {

    private final KeyboardHelper keyboardHelper;

    @Override
    public String getCommand() {
        return "/specialties";
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        send(bot, message, "🎯 <b>Спеціальності УКД</b>\n\nОберіть освітній рівень:", "specialties");
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    private void send(TelegramLongPollingBot bot, Message message, String text, String action) {
        try {
            SendMessage response = new SendMessage();
            response.setChatId(message.getChatId());
            response.setParseMode("HTML");
            response.setText(text);
            response.setReplyMarkup(keyboardHelper.catalogLevelMenu(action));
            bot.execute(response);
        } catch (TelegramApiException ignored) {
        }
    }
}
