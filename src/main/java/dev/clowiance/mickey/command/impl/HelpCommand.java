package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.CommandHelpRenderer;
import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.config.BotProperties;
import dev.clowiance.mickey.helper.KeyboardHelper;
import dev.clowiance.mickey.service.PermissionService;
import dev.clowiance.mickey.telegram.menu.MenuMessenger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class HelpCommand implements ICommand {

    private final KeyboardHelper keyboardHelper;
    private final MenuMessenger menuMessenger;
    private final CommandHelpRenderer helpRenderer;
    private final PermissionService permissionService;
    private final BotProperties botProperties;

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
        Long ownerId = message.getFrom() != null ? message.getFrom().getId() : null;
        Long chatId = message.getChatId();
        boolean inPrivate = message.getChat() != null && message.getChat().isUserChat();
        boolean developer = ownerId != null && botProperties.getDevIds() != null
                && botProperties.getDevIds().contains(ownerId);
        boolean chatAdmin = developer
                || (!inPrivate && ownerId != null && permissionService.isUserChatAdmin(bot, chatId, ownerId));

        String text = helpRenderer.render(chatAdmin, developer, inPrivate);
        menuMessenger.sendMenu(bot, chatId, ownerId, text, keyboardHelper.mainMenu());
    }
}
