package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.service.UserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

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
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        userCommandService.handleSchedule(bot, message, args);
    }
}
