package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.helper.KeyboardHelper;
import dev.clowiance.mickey.helper.TextHelper;
import dev.clowiance.mickey.telegram.menu.MenuMessenger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class MenuCommand implements ICommand {

    private final KeyboardHelper keyboardHelper;
    private final MenuMessenger menuMessenger;

    @Override
    public String getCommand() {
        return "/menu";
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
        Long ownerId = message.getFrom() != null ? message.getFrom().getId() : null;
        menuMessenger.sendMenu(bot, message.getChatId(), ownerId, TextHelper.MENU_TITLE, keyboardHelper.mainMenu());
    }
}
