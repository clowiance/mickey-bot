package dev.clowiance.mickey.command.impl;

import dev.clowiance.mickey.command.ICommand;
import dev.clowiance.mickey.service.AdminCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class UnBanCommand implements ICommand {

    private final AdminCommandService adminCommandService;

    @Override
    public String getCommand() {
        return "/unban";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public void execute(TelegramLongPollingBot bot, Message message, String args) {
        adminCommandService.handleUnban(bot, message, args);
    }
}
