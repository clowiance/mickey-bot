package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.service.UserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class ScheduleCommand implements ICommand {

    private final UserCommandService userCommandService;

    @Override
    public String getCommand() {
        return "/schedule";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isGroupOnly() {
        return false;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        if (message.getChat().isGroupChat() || message.getChat().isSuperGroupChat()) {
            try {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId());
                sendMessage.setParseMode("HTML");
                sendMessage.setText("🔒 Ця команда доступна лише в особистих повідомленнях з ботом.");
                bot.execute(sendMessage);
            } catch (TelegramApiException ignored) {
            }
            return;
        }

        userCommandService.handleSchedule(bot, message, args);
    }
}
