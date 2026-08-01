package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.CommandAccess;
import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.helper.KeyboardHelper;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class DevPanelCommand implements ICommand {

    private final KeyboardHelper keyboardHelper;

    @Override
    public String getCommand() {
        return "/devpanel";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public CommandAccess getAccessLevel() {
        return CommandAccess.DEVELOPER;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        try {
            SendMessage sm = new SendMessage();
            sm.setChatId(message.getChatId());
            sm.setParseMode("HTML");
            sm.setText(TextHelper.DEV_PANEL_TITLE);
            sm.setReplyMarkup(keyboardHelper.devPanel());
            bot.execute(sm);
        } catch (TelegramApiException ignored) {
        }
    }
}
