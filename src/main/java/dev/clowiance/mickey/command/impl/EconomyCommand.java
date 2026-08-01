package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.service.EconomyCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class EconomyCommand implements ICommand {

    private final EconomyCommandService economyCommandService;

    @Override
    public String getCommand() {
        return "/economy";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isDevOnly() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        economyCommandService.handleEconomy(bot, message, args);
    }
}
