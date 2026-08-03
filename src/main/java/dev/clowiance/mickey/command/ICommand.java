package dev.clowiance.mickey.command;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface ICommand {
    String getCommand();
    void execute(TelegramLongPollingBot bot, Message message, String args);
    boolean isAdminOnly();
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0

    default boolean isDevOnly() {
        return false;
    }

    default boolean isGroupOnly() {
        return false;
    }

    default boolean isInfoCommand() {
        return false;
    }
<<<<<<< HEAD
=======
>>>>>>> 26e0f7ae6c0171304cc5ebb93d0ea6d2410919c6
=======
>>>>>>> 2279071efff4b6931d1c5688c7106a6c800614a0
}
