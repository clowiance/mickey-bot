package dev.clowiance.mickey.command;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface ICommand {
    String getCommand();
    void execute(TelegramLongPollingBot bot, Message message, String args);
    boolean isAdminOnly();
<<<<<<< HEAD

    default boolean isDevOnly() {
        return false;
    }

    default boolean isGroupOnly() {
        return false;
    }

    default boolean isInfoCommand() {
        return false;
    }
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
}
