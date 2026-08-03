package dev.clowiance.mickey.command;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface ICommand {
    String getCommand();
    void execute(TelegramLongPollingBot bot, Message message, String args);
    boolean isAdminOnly();

    default CommandAccess access() {
        if (isDevOnly()) {
            return CommandAccess.DEVELOPER;
        }
        if (isAdminOnly()) {
            return CommandAccess.GROUP_ADMIN;
        }
        if (isInfoCommand() && !isGroupOnly()) {
            return CommandAccess.PUBLIC_INFO;
        }
        return CommandAccess.GROUP_MEMBER;
    }

    default boolean isDevOnly() {
        return false;
    }

    default boolean isGroupOnly() {
        return false;
    }

    default boolean isInfoCommand() {
        return false;
    }
}
