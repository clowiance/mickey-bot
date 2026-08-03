package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.service.ReportCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class ReportCommand implements ICommand {

    private final ReportCommandService reportCommandService;

    @Override
    public String getCommand() {
        return "/report";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isGroupOnly() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        reportCommandService.handleCreate(bot, message, args);
    }
}
