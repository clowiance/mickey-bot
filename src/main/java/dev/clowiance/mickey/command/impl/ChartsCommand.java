package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.analytics.presentation.AnalyticsTelegramService;
import dev.clowiance.mickey.command.ICommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class ChartsCommand implements ICommand {

    private final AnalyticsTelegramService analyticsTelegramService;

    @Override
    public String getCommand() {
        return "/charts";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean isGroupOnly() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        String dashboard = args == null || args.isBlank() ? "group" : args.trim().toLowerCase();
        if (!dashboard.matches("group|activity|rating|earned")) {
            dashboard = "group";
        }
        analyticsTelegramService.send(bot, message.getChatId(), dashboard);
    }
}
