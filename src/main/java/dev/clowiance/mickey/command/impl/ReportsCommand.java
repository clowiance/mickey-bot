package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.config.BotProperties;
import dev.clowiance.mickey.service.ReportCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class ReportsCommand implements ICommand {

    private final ReportCommandService reportCommandService;
    private final BotProperties botProperties;

    @Override
    public String getCommand() {
        return "/reports";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean isGroupOnly() {
        return false;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        boolean isDev = botProperties.getDevIds() != null
                && botProperties.getDevIds().contains(message.getFrom().getId());
        reportCommandService.handleListCommand(bot, message, isDev);
    }
}
