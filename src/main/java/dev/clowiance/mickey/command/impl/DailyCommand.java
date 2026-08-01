package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.service.EconomyUserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class DailyCommand implements ICommand {

    private final EconomyUserCommandService economyUserCommandService;

    @Override
    public String getCommand() {
        return "/daily";
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
        economyUserCommandService.handleDaily(bot, message);
    }
}
