package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.handler.helper.MenuImageSender;
import dev.clowiance.mickey.helper.KeyboardHelper;
import dev.clowiance.mickey.helper.TextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class HelpCommand implements ICommand {

    private final KeyboardHelper keyboardHelper;
    private final MenuImageSender menuImageSender;

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isInfoCommand() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        menuImageSender.sendMenuPhoto(bot, message.getChatId(), TextHelper.HELP_TEXT, keyboardHelper.mainMenu());
    }
}
