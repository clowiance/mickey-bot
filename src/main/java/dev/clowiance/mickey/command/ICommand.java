package dev.clowiance.mickey.command;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface ICommand {
    String getCommand();
    void execute(TelegramLongPollingBot bot, Message message, String args);
    boolean isAdminOnly();
}
