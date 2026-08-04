package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.community.presentation.StudyGroupCallbackHandler;
import dev.clowiance.mickey.community.presentation.StudyGroupMessageRenderer;
import dev.clowiance.mickey.telegram.menu.MenuMessenger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class GroupsCommand implements ICommand {

    private final StudyGroupMessageRenderer renderer;
    private final StudyGroupCallbackHandler callbackHandler;
    private final MenuMessenger menuMessenger;

    @Override
    public String getCommand() {
        return "/groups";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isGroupOnly() {
        return true;
    }

    @Override
    public boolean isInfoCommand() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        Long ownerId = message.getFrom() != null ? message.getFrom().getId() : null;
        menuMessenger.sendMenu(bot, message.getChatId(), ownerId,
                renderer.renderMenu(), callbackHandler.navigationMenu());
    }
}
